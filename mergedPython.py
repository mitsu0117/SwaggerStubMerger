def generate_placeholder(length, width):
    """
    Generate "(%s, %s, %s, ...), ..." for placing parameters.
    """
    return ','.join('(' + ','.join(['%s'] * width) + ')' for _ in range(length))


#=swagger-anchor=#
def create_sequence_subquery(length, columns):
    """Create a subquery for sequence."""
    placeholder = generate_placeholder(length, len(columns))
    subquery = 'WITH sequence {columns} AS (VALUES {placeholder})'.format(
        columns='(' + ','.join(columns) + ')',
        placeholder=placeholder)
    return subquery

#=swagger-anchor=#
#=swagger-anchor=#
def query_edges_in_sequence_bbox(conn, road_table_name, sequence, search_radius):
    """
    Query all road edges within the bounding box of the sequence
    expanded by search_radius.
    """
    if not sequence:
        return

    subquery = create_sequence_subquery(len(sequence), ('lon', 'lat'))

    stmt = subquery + '''
    -- NOTE the length unit is in km
    SELECT edge.gid, edge.source, edge.target, edge.length * 1000, edge.length * 1000
    FROM {road_table_name} AS edge
         CROSS JOIN (SELECT ST_Extent(ST_MakePoint(sequence.lon, sequence.lat))::geometry AS extent FROM sequence) AS extent
    WHERE edge.the_geom && ST_Envelope(ST_Buffer(extent.extent::geography, %s)::geometry)
    '''.format(road_table_name=road_table_name)

    # Aggregate and flatten params
    params = sum([[lon, lat] for lon, lat in sequence], [])
    params.append(search_radius)

    cur = conn.cursor()
    cur.execute(stmt, params)

    for gid, source, target, cost, reverse_cost in cur.fetchall():
        edge = Edge(id=gid,
                    start_node=source,
                    end_node=target,
                    cost=cost,
                    reverse_cost=reverse_cost)
        yield edge

    cur.close()

#=swagger-anchor=#
#=swagger-anchor=#
def build_road_network(edges):
    """Construct the bidirectional road graph given a list of edges."""
    graph = {}

    # Graph with bidirectional edges
    for edge in edges:
        graph.setdefault(edge.start_node, []).append(edge)
        graph.setdefault(edge.end_node, []).append(edge.reversed_edge())

    return graph


# Subclass the native Candidate class to support more attributes
class Candidate(mm.Candidate):
    def __init__(self, measurement, edge, location, distance):
        super(Candidate, self).__init__(measurement=measurement, edge=edge, location=location, distance=distance)
        self.lon = None
        self.lat = None


def query_candidates(conn, road_table_name, sequence, search_radius):
    """
    Query candidates of each measurement in a sequence within
    search_radius.
    """
    subquery = create_sequence_subquery(len(sequence), ('id', 'lon', 'lat'))

    subquery = subquery + ',' + '''
    --- WITH sequence AS (subquery here),
    seq AS (SELECT *,
                   ST_SetSRID(ST_MakePoint(sequence.lon, sequence.lat), 4326) AS geom,
                   ST_SetSRID(ST_MakePoint(sequence.lon, sequence.lat), 4326)::geography AS geog
        FROM sequence)
    '''

    stmt = subquery + '''
    SELECT seq.id, seq.lon, seq.lat,
           --- Edge information
           edge.gid, edge.source, edge.target,
           edge.length, edge.length,

           --- Location, a float between 0 and 1 representing the location of the closest point on the edge to the measurement.
           ST_LineLocatePoint(edge.the_geom, seq.geom) AS location,

           --- Distance in meters from the measurement to its candidate's location
           ST_Distance(seq.geog, edge.the_geom::geography) AS distance,

           --- Candidate's location (a position along the edge)
           ST_X(ST_ClosestPoint(edge.the_geom, seq.geom)) AS clon,
           ST_Y(ST_ClosestPoint(edge.the_geom, seq.geom)) AS clat

    FROM seq CROSS JOIN {road_table_name} AS edge
    WHERE edge.the_geom && ST_Envelope(ST_Buffer(seq.geog, %s)::geometry)
          AND ST_DWithin(seq.geog, edge.the_geom::geography, %s)
    '''.format(road_table_name=road_table_name)

    # Aggregate and flatten params
    params = sum([[idx, lon, lat] for idx, (lon, lat) in enumerate(sequence)], [])
    params.append(search_radius)
    params.append(search_radius)

    cur = conn.cursor()
    cur.execute(stmt, params)

    for mid, mlon, mlat, \
        eid, source, target, cost, reverse_cost, \
        location, distance, \
        clon, clat in cur:

        measurement = Measurement(id=mid, lon=mlon, lat=mlat)

        edge = Edge(id=eid, start_node=source, end_node=target, cost=cost, reverse_cost=reverse_cost)

        assert 0 <= location <= 1
        candidate = Candidate(measurement=measurement, edge=edge, location=location, distance=distance)

        # Coordinate along the edge (not needed by MM but might be
        # useful info to users)
        candidate.lon = clon
        candidate.lat = clat

        yield candidate

    cur.close()


def map_match(conn, road_table_name, sequence, search_radius, max_route_distance):
    """Match the sequence and return a list of candidates."""

    # Prepare the network graph and the candidates along the sequence
    edges = query_edges_in_sequence_bbox(conn, road_table_name, sequence, search_radius)
    network = build_road_network(edges)
    candidates = query_candidates(conn, road_table_name, sequence, search_radius)

    # If the route distance between two consive measurements are
    # longer than `max_route_distance` in meters, consider it as a
    # breakage
    matcher = mm.MapMatching(network.get, max_route_distance)

    # Match and return the selected candidates along the path
    return list(matcher.offline_match(candidates))


def parse_argv(argv):
    argv = argv[:] + [None, None]
    try:
        uri, road_table_name, search_radius, max_route_distance = argv[:4]
        search_radius = 30 if search_radius is None else int(search_radius)
        max_route_distance = 2000 if max_route_distance is None else int(max_route_distance)
    except ValueError:
        print >> sys.stderr, __doc__
        return

    return uri, road_table_name, search_radius, max_route_distance


def main(argv):
    params = parse_argv(argv)
    if not params:
        # Something is wrong
        return 1
    uri, road_table_name, search_radius, max_route_distance = params

    sequence = [map(float, line.strip().split()) for line in sys.stdin if line.strip()]

    conn = psycopg2.connect(uri)
    candidates = map_match(conn, road_table_name, sequence, search_radius, max_route_distance)
    conn.close()

    for candidate in candidates:
        print '         Measurement ID: {0}'.format(candidate.measurement.id)
        print '             Coordinate: {0:.6f} {1:.6f}'.format(*map(float, (candidate.measurement.lon, candidate.measurement.lat)))
        print '    Matche d coordinate: {0:.6f} {1:.6f}'.format(*map(float, (candidate.lon, candidate.lat)))
        print '        Matched edge ID: {0}'.format(candidate.edge.id)
        print 'Location along the edge: {0:.2f}'.format(candidate.location)
        print '               Distance: {0:.2f} meters'.format(candidate.distance)
        print

    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))

# Swagger Stub Merger
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

* [About Swagger Stub Merger](#about-swagger-stub-merger)
* [How to use](#how-to-user)


### About Swagger Stub Merger
This software enables to merge two stub programs generated by the swagger-codegen program.
If you know more about swagger-codegen, see the page bellow.
https://github.com/swagger-api/swagger-codegen

Using the swagger-codegen, a stub program based on swagger.yaml is generated and a developper can write API program with it.
However, if the yaml is needed to be modified, newly-generated stub program with the modified yaml does not contains the API programs developed in the last stub program.
To deal with this problem, this software merges the last and the new stub programs.
Stub merging processes are implemented as follow;
1. Parse directory trees of the newly generated stub programs and the last ones.
2. Create another stub directory (merged directory) and copy programs those exists only in the last ones to the merged directory.
3. Copy programs those exist in the both stub programs but do not have any differences (identical programs) to the merged directory.
4. Merge programs thosse exist in the both stub programs and have differences and put them into the merged directory.
   The merging process is implemented with annotations such as "//__swagger-anchor-start:".
   If a function with the annotation is found in the last stub, the function is substituted with the corresponding function in the new stud programs.

### How to user
```
# Clone swagger stub merger.
git clone https://github.com/mitsu0117/SwaggerStubMerger.git

# Build with gradle.
./gradlew executableJars

# Run swagger-stub-merger.jar with merging stub programs.
java -jar swagger-stub-merger.jar \
     --output-dir ./merged \
     --new-stub-root-dir ../../src/test/resources/nodejs-server-server-after \
     --current-stub-root-dir ../../src/test/resources/nodejs-server-server-before

```

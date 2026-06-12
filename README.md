This was a rendering engine I made for school, it is not intended for any real use. 
It was made as a learning excercise, which it succeeded at, but it is held back by multiple things:

    1. it was written in Java
    2. it is singlethreaded, and would take significant work to change that
    3. polygons are hardcoded (kind of) to have 4 points, making model loading much harder
    4. polygon lighting is designed to work with cubes and cubes only.
    5. no differed data is stored (ex. depth, normals, etc.)
It also has more some more general issues that could be fixed pretty easily:

    1. shadowing kind of works? but not really?
    2. anything behind the camera messes things up badly.
    3. the downsampler (superSampler.java) is very slow  
Overall I think it succeeds as a learning experience and as a party trick but not much more.
The feature set is as listed:

    1. shadowing
    2. SSAA
    3. perspective correct texture mapping
    4. lit/shadowed polygons
USAGE: (see "example.cdata")

    1. cubes.cdata is read from to load in data 
    2. each cube is formatted as x,y,z,texture.bmp,rotation of all 6 faces (value of 0-3)
    3. light source position is indentified with capital LS, followed by the x,y,z
    4. the camera postion can also be modified, stored with identifier C, followed by x,y,z
    5. reading from the file stops at a line starting with //
To run the program, first compile class files

    1. javac src/*.java
Then run the compiled App.java, with arguments name, resolution, AA level

    1. java -cp src App img 500 2
This creates 2 bmp files, one named "img.bmp" at a resolution of 1000x1000 (resolution x AA level), with no AA and imgPost.bmp, which is 500x500 with AA

This was a rendering engine I made for school, it is not intended for any real use. 

It was made as a learning excercise, which it succeeded at, but it is held back by multiple things:

    1. it was written in Java
    2. it is singlethreaded, and would take significant work to change that
    3. polygons are hardcoded (kind of) to have 4 points, making model loading much harder
    4. polygon lighting is designed to work with cubes and cubes only.
    5. no differed data is stored (ex. depth, normals, etc.)

and it has more general issues that can be fixed pretty easily

    1. shadowing kind of works? but not really?
    2. anything behind the camera messes things up badly.
    3. the downsampler (superSampler.java) is very slow
    
overall I think it succeeds as a learning experience and as a party trick but not much more.

the feature set is as listed:

    1. shadowing
    2. SSAA
    3. perspective correct texture mapping
    4. lit/shadowed polygons

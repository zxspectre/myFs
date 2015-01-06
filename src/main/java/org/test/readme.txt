Some thoughts:
1. Occurrences of RuntimeException were required for development, to catch error in a more localized scope. It is not part of the FS design.
2. Almost all of limitations (max FS size, max amount of children in directory, etc.) can be easily extended,
 but that may greatly increase amount of code without introducing new qualities of code. Although some extensions were already made
 (e.g. more than one data block for some file/dir).
3. Synchronization may be somewhat simpler that I intended to do initially, but that is due to increase of scope in FS-based logic
 (i.e. emphasis was shifted from multi-threading to more low-lvl FS workings). Also more efficient synchronization may not increase FS performance due to
 inadequate performance tuning of FS-code itself (e.g. disk caching, etc.).
4. I don't have much experience working in Java with low-lvl implementations, so coding style may be conflicting sometimes (i.e. OOP is upset sometimes).
 On the other hand, some pieces of code may perform badly (in terms of performance, when speaking about FS), but that optimization will require more effort that I think is required for this exercise.
 All in all I think there should be some balance of OOP structure in terms of classes and procedure-like some methods that can be written in a less popular way for Java (w/o getters, etc.).
5. Manual serialization was made to have control over serialized data length.
6. Exceptions handling must be reviewed. In some cases FS can remain in broken state. (try-catch every operation on high-level, try to rollback it on catch) e.g. append to file content -
 we go out of mem, then remove the file and its data blocks to rollback the failed append (not very gracious)
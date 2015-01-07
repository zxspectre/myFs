Some thoughts:
1. Occurrences of RuntimeException were required for development, to catch error in a more localized scope. It is not part of the FS design.

2. Almost all of limitations (max FS size, max amount of children in directory, etc.) can be easily extended,
 but that may greatly increase amount of code without introducing new qualities of code. Although some extensions were already made
 (e.g. more than one data block for some file/dir).

3. Synchronization may be somewhat simpler that I intended to do initially, but that is due to increase of scope in FS-based logic
 (i.e. emphasis was shifted from multi-threading to more low-lvl FS workings). Also more efficient synchronization may not increase FS performance due to
 inadequate performance tuning of FS-code itself (e.g. disk caching, etc.). In fact, current synchronization may be only marginally better than simple synchronization on instance level,
 because we are working with files, but instance-level synchronization is way too simple and was not requested for this test, in my opinion. Of course when talking about performance
 we should perform correct measurements and not just speculate, but please excuse me as I'll not be doing performance testing right now.
 =Note: current synchronization model is essentially as in ReadWriteLock. It is possible to parallelize write operations also (by partitioning data sectors in several chunks).
  It would also have good synergy with better block allocation policy.
 =Note2: current synchronization model does not provide any performance benefits over instance-level synchronization. Even worse, most likely due to overhead associated with MappedByteBuffer,
  performance is slower (up to 20%) even in case when read operations constitute around 70% of total FS calls. Numbers are approximate and may be hardware dependent (up to the point that RAID
  HDD may be actually faster in this implementation) but even so provide something to think about.

4. I don't have much experience working in Java with low-lvl implementations, so coding style may be conflicting sometimes (i.e. OOP is upset sometimes).
 On the other hand, some pieces of code may perform badly (in terms of performance, when speaking about FS), but that optimization will require more effort that I think is required for this exercise.
 All in all I think there should be some balance of OOP structure in terms of classes and procedure-like some methods that can be written in a less popular way for Java (w/o getters, etc.).

5. Manual serialization was made to have control over serialized data length.

6. Exceptions handling must be reviewed. Some operations try to rollback change if it does not complete successfully (in order not to leave FS in broken state).
Basic scenarios are covered by test cases.

7. Exceptions hierarchy should be extended (too much situations are handled by StorageException)
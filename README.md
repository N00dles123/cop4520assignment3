# COP4520 Assignment 3
Jason Lin COP 4520
## Compilation and Running in Command Line
To compile The Birthday Presents Party, cd to the file directory of birthdayPresents.java, then type in "javac birthdayPresents.java". To run the program type in "java birthdayPresents <true or false>" in order to see whether you want output excluding the amount of time to run the program to be printed, by default its nothing will be printed other than the time it takes to run the program. You can also put the output into a .txt file and do "java birthdayPresents true > output.txt. <br/>
To compile Atmospheric Temperature Reading Module, cd to the file directory of atmosphericTemp.java, then type in "javac atmosphericTemp.java". To run the program type in "java atmosphericTemp.

## Problem 1: The Birthday Presents Party solution and proof of correctness
In this program, the ConcurLinkedList class implemented a concurrent linked list which also included a lock or mutex in order to enforce mutual exclusion on critical sections. The linkedlist class included these following functions to support operations on linked lists: add, remove, contains, pop and they can be accessed by multiple threads. The linked list implemented will then be used in a variable called list which stores the "chain". The unordered bag is created through the use of a linked list and then it is shuffled using Collections.shuffle and the servants can pop this linked list to get the values in the front of the list each time and then write a thank you card. Finally, we have a set which then is checked to see whether all the gifts have been processed. <br/>
Then, each thread representing a servant chooses a random integer from [0 - 2], this will represent the actions each servant will be doing at a time. 0 represents adding a gift from the unordered bag to the to the chain in the correct order. 1 represents checkign whether a gift with a particular tag was present in the chain or not (meaning it checks the thank you card set as well because that means it was present in the chain at a certain time). 2 represents writing a thank you card and this pops the head of the linked list and adds the guest to the set of thank you cards signifying that the card was been written. 
 


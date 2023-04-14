# Assignment 3

## Minotaur
### Problem
There are 3 possible actions
1. Move present from queue to linked list
2. Remove present from the linked list and write a thank you note
3. Search for a present in the concurrent linked list by tag

### Solution
Create a custom concurrent linked list class that uses presents as nodes and is able to add, remove, and search through it. Create a ServantAction thread class representing each servant and randomly choose a task and complete it. Continue until we've finshed all of the presents in the bag and written all the thank-you notes. 

### Proof of correctness
The concurrent linked list uses fine-grained locks which ensures correctness. Fine-grained locks allow multiple threads to safely access and modify the list concurrently. BlockingQueue also ensures safe and efficient modification and access for the present and thank you notes

### Testing
This code was tested with the desired case of 4 threads and 500,000 presents and runs in roughly 800 ms. 

## MarsRover
### Problem
Periodically take temperature readings and then every hour compute the top 5 highest and top 5 lowest temperatures that have been read so far. Additionally, compute the largest temperature difference and report this information. 

### Solution
Create a custom Report data structure with the capability of storing many temperature readings, getting the top 5 highest and lowest temperatures, maximum temperature difference, and adding new temperatures. Use this report data structure with 8 threads (made with a custom Sensor class) to store generated temperature readings periodically. 

### Proof of correctness
The MarsRover class manages all the threads, the ReadWriteLock class ensures thread-safe access to the shared data structure TemperatureData. This ensures correctness and thread safety. 

### Evaluation 
This solution is flexible in simulation and speed and will depend on how the user utilizes it for runtime. 
import java.util.*;

public class RoundRobin extends CPUScheduler {
    @Override
    public void process() {
        // Sort processes by arrival time
        Collections.sort(this.getRows(), Comparator.comparingInt(Row::getArrivalTime));
        
        // Create a copy of rows to work with
        List<Row> rows = new ArrayList<>(this.getRows());
        
        // Initialize variables
        int currentTime = rows.get(0).getArrivalTime();
        int timeQuantum = this.getTimeQuantum();
        
        // Track remaining burst times
        Map<String, Integer> remainingTime = new HashMap<>();
        for (Row row : rows) {
            remainingTime.put(row.getProcessName(), row.getBurstTime());
        }
        
        // Ready queue to manage process execution
        Queue<Row> readyQueue = new LinkedList<>();
        
        // Add first process to ready queue
        readyQueue.offer(rows.get(0));
        
        // Keep track of finished and unfinished processes
        List<Row> unfinishedJobs = new ArrayList<>(rows);
        List<Row> solvedProcesses = new ArrayList<>();
        
        while (!unfinishedJobs.isEmpty()) {
            // Handle case when ready queue is empty
            if (readyQueue.isEmpty() && !unfinishedJobs.isEmpty()) {
                Row nextProcess = unfinishedJobs.get(0);
                readyQueue.offer(nextProcess);
                currentTime = nextProcess.getArrivalTime();
            }
            
            Row currentProcess = readyQueue.poll();
            int processRemainingTime = remainingTime.get(currentProcess.getProcessName());
            
            // Determine execution time
            int executionTime = Math.min(processRemainingTime, timeQuantum);
            
            // Add event to timeline
            this.getTimeline().add(new Event(currentProcess.getProcessName(), currentTime, currentTime + executionTime));
            
            // Update current time and remaining time
            currentTime += executionTime;
            processRemainingTime -= executionTime;
            remainingTime.put(currentProcess.getProcessName(), processRemainingTime);
            
            // Add newly arrived processes to ready queue
            for (Iterator<Row> iterator = unfinishedJobs.iterator(); iterator.hasNext();) {
                Row process = iterator.next();
                if (process.getArrivalTime() <= currentTime && process != currentProcess && !readyQueue.contains(process)) {
                    readyQueue.offer(process);
                }
            }
            
            // Handle process completion
            if (processRemainingTime == 0) {
                unfinishedJobs.remove(currentProcess);
                
                // Calculate waiting and turnaround times
                int waitingTime = calculateWaitingTime(currentProcess, this.getTimeline());
                currentProcess.setWaitingTime(waitingTime);
                currentProcess.setTurnaroundTime(waitingTime + currentProcess.getBurstTime());
                
                solvedProcesses.add(currentProcess);
            } else {
                // Re-queue the process if not completed
                readyQueue.offer(currentProcess);
            }
        }
        
        // Optional: Sort solved processes by arrival time and process name
        solvedProcesses.sort(Comparator.comparingInt(Row::getArrivalTime)
                .thenComparing(Row::getProcessName));
    }
    
    // Helper method to calculate waiting time
    private int calculateWaitingTime(Row process, List<Event> timeline) {
        // Find first and last occurrences of the process
        int firstOccurrence = Integer.MAX_VALUE;
        int lastOccurrence = 0;
        
        for (Event event : timeline) {
            if (event.getProcessName().equals(process.getProcessName())) {
                firstOccurrence = Math.min(firstOccurrence, event.getStartTime());
                lastOccurrence = Math.max(lastOccurrence, event.getFinishTime());
            }
        }
        
        return firstOccurrence - process.getArrivalTime();
    }
}
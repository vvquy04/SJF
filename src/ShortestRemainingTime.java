import java.util.*;
import java.util.stream.Collectors;

public class ShortestRemainingTime extends CPUScheduler {
    @Override
    public void process() {
        // Sort processes by arrival time and then by burst time
        Collections.sort(this.getRows(), (a, b) -> {
            int arrivalComparison = Integer.compare(a.getArrivalTime(), b.getArrivalTime());
            if (arrivalComparison != 0) return arrivalComparison;
            return Integer.compare(a.getBurstTime(), b.getBurstTime());
        });
        
        List<Row> unfinishedJobs = new ArrayList<>(this.getRows());
        List<Row> solvedProcesses = new ArrayList<>();
        
        // Track remaining times for each process
        Map<String, Integer> remainingTime = new HashMap<>();
        for (Row row : unfinishedJobs) {
            remainingTime.put(row.getProcessName(), row.getBurstTime());
        }
        
        // Ready queue to manage process execution
        List<Row> readyQueue = new ArrayList<>();
        
        int currentTime = unfinishedJobs.get(0).getArrivalTime();
        readyQueue.add(unfinishedJobs.get(0));
        
        while (!unfinishedJobs.isEmpty()) {
            // Handle idle time scenario
            boolean wasIdle = false;
            if (readyQueue.isEmpty() && !unfinishedJobs.isEmpty()) {
                wasIdle = true;
                readyQueue.add(unfinishedJobs.get(0));
                currentTime = readyQueue.get(0).getArrivalTime();
            }
            
            // Sort ready queue by remaining time
            readyQueue.sort((a, b) -> 
                Integer.compare(remainingTime.get(a.getProcessName()), 
                                remainingTime.get(b.getProcessName()))
            );
            
            Row currentProcess = readyQueue.get(0);
            
            // Find processes that can arrive during current process execution
            List<Row> arrivingProcesses = findArrivingProcesses(
                unfinishedJobs, 
                currentProcess, 
                currentTime, 
                remainingTime, 
                readyQueue, 
                wasIdle
            );
            
            // Determine execution details
            int executionAmount = determineExecutionAmount(
                currentProcess, 
                arrivingProcesses, 
                remainingTime, 
                currentTime, 
                wasIdle
            );
            
            // Update timeline and current time
            updateTimeline(
                this.getTimeline(), 
                currentProcess, 
                currentTime, 
                executionAmount
            );
            
            currentTime += executionAmount;
            
            // Update remaining time
            int processRemainingTime = remainingTime.get(currentProcess.getProcessName());
            processRemainingTime -= executionAmount;
            remainingTime.put(currentProcess.getProcessName(), processRemainingTime);
            
            // Add newly arrived processes
            addNewProcessesToQueue(
                unfinishedJobs, 
                readyQueue, 
                currentTime, 
                currentProcess
            );
            
            // Handle process completion
            if (processRemainingTime == 0) {
                completeProcess(
                    unfinishedJobs, 
                    readyQueue, 
                    solvedProcesses, 
                    currentProcess, 
                    currentTime, 
                    this.getRows()
                );
            } else {
                // Requeue the process
                readyQueue.add(readyQueue.remove(0));
            }
        }
        
        // Calculate waiting and turnaround times
        calculateProcessTimes(this.getRows(), this.getTimeline());
    }
    
    // Helper methods (implementation details would go here)
    private List<Row> findArrivingProcesses(
        List<Row> unfinishedJobs, 
        Row currentProcess, 
        int currentTime, 
        Map<String, Integer> remainingTime, 
        List<Row> readyQueue, 
        boolean wasIdle
    ) {
        return unfinishedJobs.stream()
            .filter(p -> isProcessEligibleToArrive(
                p, currentProcess, currentTime, remainingTime, readyQueue, wasIdle))
            .collect(Collectors.toList());
    }
    
    private boolean isProcessEligibleToArrive(
        Row p, 
        Row currentProcess, 
        int currentTime, 
        Map<String, Integer> remainingTime, 
        List<Row> readyQueue, 
        boolean wasIdle
    ) {
        int effectiveCurrentTime = wasIdle ? currentProcess.getArrivalTime() : currentTime;
        return p.getArrivalTime() <= effectiveCurrentTime + 
               remainingTime.get(currentProcess.getProcessName()) &&
               p != currentProcess &&
               !readyQueue.contains(p) &&
               remainingTime.containsKey(p.getProcessName());
    }
    
    private int determineExecutionAmount(
        Row currentProcess, 
        List<Row> arrivingProcesses, 
        Map<String, Integer> remainingTime, 
        int currentTime, 
        boolean wasIdle
    ) {
        if (arrivingProcesses.isEmpty()) {
            return remainingTime.get(currentProcess.getProcessName());
        }
        
        Row shortestArrivingProcess = arrivingProcesses.stream()
            .min(Comparator.comparingInt(Row::getBurstTime))
            .orElse(null);
        
        if (shortestArrivingProcess == null) {
            return remainingTime.get(currentProcess.getProcessName());
        }
        
        int effectiveCurrentTime = wasIdle ? currentProcess.getArrivalTime() : currentTime;
        return Math.min(
            shortestArrivingProcess.getArrivalTime() - effectiveCurrentTime, 
            remainingTime.get(currentProcess.getProcessName())
        );
    }
    
    private void updateTimeline(
        List<Event> timeline, 
        Row currentProcess, 
        int currentTime, 
        int executionAmount
    ) {
        timeline.add(new Event(
            currentProcess.getProcessName(), 
            currentTime, 
            currentTime + executionAmount
        ));
    }
    
    private void addNewProcessesToQueue(
        List<Row> unfinishedJobs, 
        List<Row> readyQueue, 
        int currentTime, 
        Row currentProcess
    ) {
        unfinishedJobs.stream()
            .filter(p -> p.getArrivalTime() <= currentTime && 
                         p != currentProcess && 
                         !readyQueue.contains(p))
            .forEach(readyQueue::add);
    }
    
    private void completeProcess(
        List<Row> unfinishedJobs, 
        List<Row> readyQueue, 
        List<Row> solvedProcesses, 
        Row currentProcess, 
        int currentTime, 
        List<Row> originalRows
    ) {
        unfinishedJobs.remove(currentProcess);
        readyQueue.remove(currentProcess);
    }
    
    private void calculateProcessTimes(List<Row> originalRows, List<Event> timeline) {
        for (Row row : originalRows) {
            List<Event> processEvents = timeline.stream()
                .filter(e -> e.getProcessName().equals(row.getProcessName()))
                .collect(Collectors.toList());
            
            if (!processEvents.isEmpty()) {
                int firstEvent = processEvents.get(0).getStartTime();
                int lastEvent = processEvents.get(processEvents.size() - 1).getFinishTime();
                
                row.setWaitingTime(firstEvent - row.getArrivalTime());
                row.setTurnaroundTime(lastEvent - row.getArrivalTime());
            }
        }
    }
}
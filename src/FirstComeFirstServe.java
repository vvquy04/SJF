import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstComeFirstServe extends CPUScheduler {

    @Override
    public void process() {
        if (this.getRows().isEmpty()) {
            throw new IllegalArgumentException("No processes available to schedule.");
        }

        // Sắp xếp theo thời gian đến
        Collections.sort(this.getRows(), (o1, o2) -> Integer.compare(o1.getArrivalTime(), o2.getArrivalTime()));

        List<Row> rows = this.getRows();
        List<Event> timeline = this.getTimeline();

        int[] finishTime = new int[rows.size()]; // Mảng lưu thời gian hoàn thành
        int currentTime = rows.get(0).getArrivalTime(); // Bắt đầu từ thời gian đến đầu tiên

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);

            if (currentTime < row.getArrivalTime()) {
                // Nếu CPU rảnh, nhảy đến thời gian đến của tiến trình tiếp theo
                currentTime = row.getArrivalTime();
            }

            // Tính thời gian kết thúc
            finishTime[i] = currentTime + row.getBurstTime();

            // Thêm tiến trình vào biểu đồ Gantt
            timeline.add(new Event(row.getProcessName(), currentTime, finishTime[i]));

            // Cập nhật thời gian hiện tại
            currentTime = finishTime[i];
        }

        // Cập nhật Waiting Time (WT) và Turnaround Time (TAT)
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.setTurnaroundTime(finishTime[i] - row.getArrivalTime()); // TAT = FT - AT
            row.setWaitingTime(row.getTurnaroundTime() - row.getBurstTime()); // WT = TAT - BT
        }
    }
}

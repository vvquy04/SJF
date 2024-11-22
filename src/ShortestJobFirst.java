import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShortestJobFirst extends CPUScheduler {

    @Override
    public void process() {
        if (this.getRows().isEmpty()) {
            throw new IllegalArgumentException("No processes available to schedule.");
        }

        // Sắp xếp các tiến trình theo thời gian đến
        Collections.sort(this.getRows(), (o1, o2) -> Integer.compare(o1.getArrivalTime(), o2.getArrivalTime()));

        List<Row> rows = Utility.deepCopy(this.getRows());
        int time = rows.get(0).getArrivalTime();

        while (!rows.isEmpty()) {
            // Lấy danh sách các tiến trình sẵn sàng
            List<Row> availableRows = getAvailableRows(rows, time);

            // Nếu không có tiến trình sẵn sàng, nhảy đến thời gian đến của tiến trình kế tiếp
            if (availableRows.isEmpty()) {
                time = rows.get(0).getArrivalTime();
                continue;
            }

            // Chọn tiến trình có thời gian thực thi ngắn nhất
            Row shortestJob = getShortestJob(availableRows);

            // Thêm sự kiện vào timeline và cập nhật thời gian
            this.getTimeline().add(new Event(shortestJob.getProcessName(), time, time + shortestJob.getBurstTime()));
            time += shortestJob.getBurstTime();

            // Xóa tiến trình đã thực thi khỏi danh sách
            rows.removeIf(r -> r.getProcessName().equals(shortestJob.getProcessName()));
        }

        // Tính toán thời gian chờ (waiting time) và thời gian hoàn thành (turnaround time)
        calculateTimes();
    }

    // Lấy danh sách các tiến trình sẵn sàng tại thời điểm hiện tại
    private List<Row> getAvailableRows(List<Row> rows, int time) {
        List<Row> availableRows = new ArrayList<>();
        for (Row row : rows) {
            if (row.getArrivalTime() <= time) {
                availableRows.add(row);
            }
        }
        return availableRows;
    }

    // Lấy tiến trình có thời gian thực thi ngắn nhất từ danh sách sẵn sàng
    private Row getShortestJob(List<Row> availableRows) {
        return Collections.min(availableRows, (o1, o2) -> Integer.compare(o1.getBurstTime(), o2.getBurstTime()));
    }

    // Tính toán thời gian chờ và thời gian hoàn thành cho từng tiến trình
    private void calculateTimes() {
        for (Row row : this.getRows()) {
            Event event = this.getEvent(row);
            row.setWaitingTime(event.getStartTime() - row.getArrivalTime());
            row.setTurnaroundTime(row.getWaitingTime() + row.getBurstTime());
        }
    }
}

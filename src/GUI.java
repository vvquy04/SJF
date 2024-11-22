
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class GUI
{
    private JFrame frame;
    private JPanel mainPanel;
    private CustomPanel chartPanel;
    private JScrollPane tablePane;
    private JScrollPane chartPane;
    private JTable table;
    private JButton addBtn;
    private JButton removeBtn;
    private JButton computeBtn;
    private JLabel wtLabel;
    private JLabel wtResultLabel;
    private JLabel tatLabel;
    private JLabel tatResultLabel;
    private JComboBox option;
    private DefaultTableModel model;
    
    public GUI()
    {
        
        model = new DefaultTableModel(new String[]{"Tiến Trình", "Thời Gian Xuất Hiện", "Thời Gian Thực Hiện", "Thời Gian Chờ", "Thời Gian Hoàn Thành"}, 0);
        
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        tablePane = new JScrollPane(table);
        tablePane.setBounds(25, 25, 450, 250);
        
        addBtn = new JButton("Add");
        addBtn.setBounds(300, 280, 85, 25);
        addBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                model.addRow(new String[]{"", "", "", "", "", ""});
            } 
        });
        
        removeBtn = new JButton("Remove");
        removeBtn.setBounds(390, 280, 85, 25);
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeBtn.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                
                if (row > -1) {
                    model.removeRow(row);
                }
            }
        });
        
        chartPanel = new CustomPanel();
//        chartPanel.setPreferredSize(new Dimension(700, 10));
        chartPanel.setBackground(Color.WHITE);
        chartPane = new JScrollPane(chartPanel);
        chartPane.setBounds(25, 310, 450, 100);
        
        wtLabel = new JLabel("Thời gian chờ trung bình:");
        wtLabel.setBounds(25, 425, 180, 25);
        tatLabel = new JLabel("Thời gian hoàn thành trung bình:");
        tatLabel.setBounds(25, 450, 180, 25);

        wtResultLabel = new JLabel();
        wtResultLabel.setBounds(215, 425, 180, 25);
        tatResultLabel = new JLabel();
        tatResultLabel.setBounds(215, 450, 180, 25);
        
        option = new JComboBox(new String[]{"FCFS", "SJF", "SRT", "RR"});
        option.setBounds(390, 420, 85, 20);
        
        computeBtn = new JButton("Compute");
        computeBtn.setBounds(390, 450, 85, 25);
        computeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        computeBtn.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selected = (String) option.getSelectedItem();
            CPUScheduler scheduler;

            switch (selected) {
                case "FCFS":
                    scheduler = new FirstComeFirstServe();
                    break;
                case "SJF":
                    scheduler = new ShortestJobFirst();
                    break;
                case "SRT":
                    scheduler = new ShortestRemainingTime();
                    break;
                case "RR":
                    String tq = JOptionPane.showInputDialog("Time Quantum");
                    if (tq == null || tq.trim().isEmpty()) {  // Kiểm tra chuỗi rỗng
                        JOptionPane.showMessageDialog(null, "Time Quantum không thể để trống.");
                        return;
                    }
                    try {
                        scheduler = new RoundRobin();
                        scheduler.setTimeQuantum(Integer.parseInt(tq));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Vui lòng nhập một số hợp lệ cho Time Quantum.");
                        return;
                    }
                    break;
                default:
                    return;
            }

            for (int i = 0; i < model.getRowCount(); i++) {
                String process = (String) model.getValueAt(i, 0);
                String arrivalTimeStr = (String) model.getValueAt(i, 1);
                String burstTimeStr = (String) model.getValueAt(i, 2);
                if (process.isEmpty() || arrivalTimeStr.isEmpty() || burstTimeStr.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Tất cả các trường thông tin tiến trình phải được điền.");
                    return;
                }

                try {
                    int at = Integer.parseInt(arrivalTimeStr);
                    int bt = Integer.parseInt(burstTimeStr);
                    scheduler.add(new Row(process, at, bt));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Vui lòng nhập số hợp lệ cho Arrival Time và Burst Time.");
                    return;
                }
            }

            scheduler.process();

            for (int i = 0; i < model.getRowCount(); i++) {
                String process = (String) model.getValueAt(i, 0);
                Row row = scheduler.getRow(process);
                model.setValueAt(row.getWaitingTime(), i, 3); // Sửa chỉ số cột cho đúng
                model.setValueAt(row.getTurnaroundTime(), i, 4); // Sửa chỉ số cột cho đúng
            }

            wtResultLabel.setText(Double.toString(scheduler.getAverageWaitingTime()));
            tatResultLabel.setText(Double.toString(scheduler.getAverageTurnAroundTime()));
            chartPanel.setTimeline(scheduler.getTimeline());
        }
    });

        
        mainPanel = new JPanel(null);
        mainPanel.setPreferredSize(new Dimension(500, 500));
        mainPanel.add(tablePane);
        mainPanel.add(addBtn);
        mainPanel.add(removeBtn);
        mainPanel.add(chartPane);
        mainPanel.add(wtLabel);
        mainPanel.add(tatLabel);
        mainPanel.add(wtResultLabel);
        mainPanel.add(tatResultLabel);
        mainPanel.add(option);
        mainPanel.add(computeBtn);
        
        frame = new JFrame("CPU Scheduler Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        frame.add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }
    
    public static void main(String[] args)
    {
        new GUI();
    }
    
    class CustomPanel extends JPanel
    {   
        private List<Event> timeline;
        
        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            if (timeline != null)
            {
//                int width = 30;
                
                for (int i = 0; i < timeline.size(); i++)
                {
                    Event event = timeline.get(i);
                    int x = 30 * (i + 1);
                    int y = 20;
                    
                    g.drawRect(x, y, 30, 30);
                    g.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g.drawString(event.getProcessName(), x + 10, y + 20);
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g.drawString(Integer.toString(event.getStartTime()), x - 5, y + 45);
                    
                    if (i == timeline.size() - 1)
                    {
                        g.drawString(Integer.toString(event.getFinishTime()), x + 27, y + 45);
                    }
                    
//                    width += 30;
                }
                
//                this.setPreferredSize(new Dimension(width, 75));
            }
        }
        
        public void setTimeline(List<Event> timeline)
        {
            this.timeline = timeline;
            repaint();
        }
    }
}

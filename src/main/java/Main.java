import com.fazecast.jSerialComm.SerialPort;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Main {

    private static SerialPort port;

    private static int speed = 0;
    private static char currentDirection = 'S';

    private static JLabel speedLabel;
    private static JLabel statusLabel;
    private static JSlider speedSlider;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(Main::createWindow);
    }

    private static void createWindow() {

        // =========================
        // CONNECT TO ARDUINO
        // =========================

        port = SerialPort.getCommPort("COM3");
        port.setBaudRate(9600);

        if (!port.openPort()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not connect to Arduino on COM3.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Give Arduino time to reset after opening COM3
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // =========================
        // WINDOW
        // =========================

        JFrame frame = new JFrame("RC Car Controller");

        frame.setSize(500, 650);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout(10, 10));

        // =========================
        // TITLE
        // =========================

        JLabel title = new JLabel(
                "RC CAR CONTROLLER",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 28));

        frame.add(title, BorderLayout.NORTH);

        // =========================
        // MAIN PANEL
        // =========================

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // =========================
        // SPEED
        // =========================

        speedLabel = new JLabel(
                "Speed: 0%",
                SwingConstants.CENTER
        );

        speedLabel.setFont(new Font("Arial", Font.BOLD, 24));
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(speedLabel);

        // =========================
        // SPEED SLIDER
        // =========================

        speedSlider = new JSlider(
                JSlider.HORIZONTAL,
                0,
                100,
                0
        );

        speedSlider.setMajorTickSpacing(25);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        speedSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        speedSlider.addChangeListener(e -> {

            speed = speedSlider.getValue();

            speedLabel.setText("Speed: " + speed + "%");

            // If the car is currently moving,
            // immediately update its speed.
            if (currentDirection == 'B' ||
                    currentDirection == 'F') {

                sendCommand(
                        currentDirection + "," + speed
                );
            }
        });

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(speedSlider);

        // =========================
        // FORWARD BUTTON
        // =========================

        JButton forwardButton = new JButton("▲  BACKWARD");

        forwardButton.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        forwardButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        forwardButton.addActionListener(e -> {

            currentDirection = 'B';

            sendCommand(
                    "B," + speed
            );
        });

        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(forwardButton);

        // =========================
        // LEFT / STOP / RIGHT
        // =========================

        JPanel middlePanel = new JPanel(
                new GridLayout(1, 3, 10, 10)
        );

        JButton leftButton = new JButton("◀ LEFT");
        JButton stopButton = new JButton("STOP");
        JButton rightButton = new JButton("RIGHT ▶");

        leftButton.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        stopButton.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        rightButton.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        // LEFT
        leftButton.addActionListener(e -> {
            sendCommand("L");
        });

        // RIGHT
        rightButton.addActionListener(e -> {
            sendCommand("R");
        });

        // STOP
        stopButton.addActionListener(e -> {

            currentDirection = 'S';

            speed = 0;
            speedSlider.setValue(0);

            sendCommand("S");
        });

        middlePanel.add(leftButton);
        middlePanel.add(stopButton);
        middlePanel.add(rightButton);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(middlePanel);

        // =========================
        // BACKWARD BUTTON
        // =========================

        JButton backwardButton = new JButton("▼  FORWARD");

        backwardButton.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        backwardButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backwardButton.addActionListener(e -> {

            currentDirection = 'F';

            sendCommand(
                    "F," + speed
            );
        });

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(backwardButton);

        // =========================
        // STATUS
        // =========================

        statusLabel = new JLabel(
                "Connected to Arduino",
                SwingConstants.CENTER
        );

        statusLabel.setFont(
                new Font("Arial", Font.BOLD, 16)
        );

        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createVerticalStrut(30));
        mainPanel.add(statusLabel);

        frame.add(mainPanel, BorderLayout.CENTER);

        // =========================
        // CLOSE WINDOW SAFELY
        // =========================

        frame.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(
                            java.awt.event.WindowEvent e) {

                        sendCommand("S");

                        if (port != null &&
                                port.isOpen()) {

                            port.closePort();
                        }
                    }
                }
        );

        frame.setVisible(true);
    }

    // =========================
    // SEND COMMAND
    // =========================

    private static void sendCommand(String command) {

        if (port == null || !port.isOpen()) {
            statusLabel.setText(
                    "Arduino is not connected!"
            );
            return;
        }

        try {

            String message = command + "\n";

            port.getOutputStream().write(
                    message.getBytes()
            );

            port.getOutputStream().flush();

            statusLabel.setText(
                    "Sent: " + command
            );

            System.out.println(
                    "Sent: " + command
            );

        } catch (Exception e) {

            statusLabel.setText(
                    "Error sending command"
            );

            System.out.println(
                    "Error: " + e.getMessage()
            );
        }
    }
}
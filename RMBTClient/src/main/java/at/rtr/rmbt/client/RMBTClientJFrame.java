/*******************************************************************************
 * Copyright 2015 SPECURE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package at.rtr.rmbt.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class RMBTClientJFrame extends JFrame {

    private ClientMode clientMode = ClientMode.NORMAL;
    private JToolBar toolBar;
    private JButton startButton;
    private JCheckBox openBrowserCheckbox;
    private JCheckBox runQoSCheckbox;
    private JCheckBox loopModeCheckbox;
    private JLabel countLabel;
    private JSpinner countSpinner;
    private JLabel intervalLabel;
    private JSpinner intervalSpinner;
    private JTextArea textArea;
    // output stream redirection
    private JTextAreaOutputStream outputStream;

    public RMBTClientJFrame(String applicationName, ClientMode clientMode) {
        super(applicationName);

        this.clientMode = clientMode;

        // init components
        initComponents();

        // output stream
        outputStream = new JTextAreaOutputStream(textArea);

        // set default close operation = exit
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLocationByPlatform(true);
    }

    private void initComponents() {

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        if (clientMode.equals(ClientMode.DEV_MODE)) {

            // toolbar
            toolBar = new JToolBar();

            // start button
            startButton = new JButton("Start Test");
            startButton.setPreferredSize(new Dimension(80, 25));
            startButton.setMaximumSize(new Dimension(80, 25));
            toolBar.add(startButton);

            // open browser
            openBrowserCheckbox = new JCheckBox("Show Result", false);
            openBrowserCheckbox.setToolTipText("Show Test result in Browser");
            toolBar.add(openBrowserCheckbox);

            // run QoS tests
            runQoSCheckbox = new JCheckBox("QoS Tests", false);
            runQoSCheckbox.setToolTipText("Perform Quality of Service Tests also");
            toolBar.add(runQoSCheckbox);

            // enable loop mode checkbox
            loopModeCheckbox = new JCheckBox("Loop Mode", false);
            loopModeCheckbox.setToolTipText("Run tests in Loop Mode, with defined Count of Loops and Interval(in minutes) between two loops");
            loopModeCheckbox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    countLabel.setVisible(loopModeCheckbox.isSelected());
                    countSpinner.setVisible(loopModeCheckbox.isSelected());
                    intervalLabel.setVisible(loopModeCheckbox.isSelected());
                    intervalSpinner.setVisible(loopModeCheckbox.isSelected());
                }
            });
            toolBar.add(loopModeCheckbox);

            // count of loops
            countLabel = new JLabel(" Loops: ");
            countLabel.setVisible(false);
            countLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            toolBar.add(countLabel);

            countSpinner = new JSpinner(new SpinnerNumberModel(10, 2, 1000, 1));
            countSpinner.setToolTipText("Count of Loops");
            countSpinner.setVisible(false);
            countSpinner.setPreferredSize(new Dimension(50, 25));
            countSpinner.setMaximumSize(new Dimension(50, 25));
            countSpinner.setValue(10);
            toolBar.add(countSpinner);

            // interval between loops
            intervalLabel = new JLabel(" Interval: ");
            intervalLabel.setVisible(false);
            intervalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            toolBar.add(intervalLabel);

            intervalSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1440, 1));
            intervalSpinner.setToolTipText("Interval between two loops in minutes");
            intervalSpinner.setVisible(false);
            intervalSpinner.setPreferredSize(new Dimension(50, 25));
            intervalSpinner.setMaximumSize(new Dimension(50, 25));
            intervalSpinner.setValue(1);
            toolBar.add(intervalSpinner);

            // add toolbar to frame
            contentPane.add(toolBar, BorderLayout.NORTH);

        }

        // text area
        textArea = new JTextArea();
        textArea.setEditable(false);

        contentPane.add(
                new JScrollPane(
                        textArea,
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);

        setMinimumSize(new Dimension(640, 480));
        setPreferredSize(new Dimension(640, 480));
        setMaximumSize(new Dimension(640, 480));

        pack();
        validate();
    }

    public void enableComponents() {
        startButton.setEnabled(true);
        openBrowserCheckbox.setEnabled(true);
        runQoSCheckbox.setEnabled(true);
        loopModeCheckbox.setEnabled(true);
        countLabel.setEnabled(true);
        countSpinner.setEnabled(true);
        intervalLabel.setEnabled(true);
        intervalSpinner.setEnabled(true);
    }

    public void disableComponents() {
        startButton.setEnabled(true);
        openBrowserCheckbox.setEnabled(false);
        runQoSCheckbox.setEnabled(false);
        loopModeCheckbox.setEnabled(false);
        countLabel.setEnabled(false);
        countSpinner.setEnabled(false);
        intervalLabel.setEnabled(false);
        intervalSpinner.setEnabled(false);
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JTextAreaOutputStream getOutputStream() {
        return outputStream;
    }

    public boolean openBrowser() {
        return openBrowserCheckbox.isSelected();
    }

    public boolean runQoS() {
        return runQoSCheckbox.isSelected();
    }

    public boolean isLoopModeEnabled() {
        return loopModeCheckbox.isSelected();
    }

    public int countOfLoops() {
        return (int) countSpinner.getValue();
    }

    public int intervalInMiliseconds() {
        return (int) intervalSpinner.getValue() * 60000;
    }

    public enum ClientMode {
        NORMAL, DEV_MODE
    }
}
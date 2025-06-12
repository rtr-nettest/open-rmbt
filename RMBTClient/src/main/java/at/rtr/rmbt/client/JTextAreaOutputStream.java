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
import java.io.IOException;
import java.io.OutputStream;

public class JTextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;

    public JTextAreaOutputStream(JTextArea textArea) {
        if (textArea == null)
            throw new IllegalArgumentException("Destination is null");

        this.textArea = textArea;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        if (SwingUtilities.isEventDispatchThread()) {
            textArea.append(text);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textArea.append(text);
                }
            });
        }
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

}
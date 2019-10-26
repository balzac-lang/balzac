/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.ui.preferences;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.google.common.base.Throwables;
import com.google.inject.Injector;

import xyz.balzaclang.balzac.ui.internal.BalzacActivator;
import xyz.balzaclang.lib.client.BitcoinClient;
import xyz.balzaclang.lib.client.impl.RPCBitcoinClient;
import xyz.balzaclang.utils.BitcoinClientFactory;
import xyz.balzaclang.utils.SecureStorageUtils;

public class TrustedNodesPreferences extends PreferencePage implements IWorkbenchPreferencePage {
    private Text testnetHostText;
    private Spinner testnetPortSpinner;
    private Button testnetHttpsCheckbox;
    private Text testnetUsernameText;
    private Text testnetUrlText;
    private Text testnetPasswordText;
    private Spinner testnetTimeoutSpinner;

    private Text mainnetHostText;
    private Spinner mainnetPortSpinner;
    private Button mainnetHttpsCheckbox;
    private Text mainnetUsernameText;
    private Text mainnetUrlText;
    private Text mainnetPasswordText;
    private Spinner mainnetTimeoutSpinner;

    private ISecurePreferences secureStorage;

    private ImageDescriptor imageErrorDesc;
    private ImageDescriptor imageSuccessDesc;
    private Image imageError;
    private Image imageSuccess;

    /**
     * Create the preference page.
     */
    public TrustedNodesPreferences() {
        setDescription("Configure your trusted Bitcoin nodes to enable further static checks of your code");
        setTitle("Trusted Nodes");
        setPreferenceStore(BalzacActivator.getInstance().getPreferenceStore());
    }

    /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        secureStorage = SecurePreferencesFactory.getDefault();
        imageErrorDesc = BalzacActivator.imageDescriptorFromPlugin("xyz.balzaclang.balzac.ui", "images/error.gif");
        imageSuccessDesc = BalzacActivator.imageDescriptorFromPlugin("xyz.balzaclang.balzac.ui",
                "images/success.gif");
        imageError = imageErrorDesc.createImage();
        imageSuccess = imageSuccessDesc.createImage();
    }

    /**
     * Create contents of the preference page.
     *
     * @param parent
     *            the parent composite
     */
    @Override
    public Control createContents(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout(SWT.VERTICAL));

        // testnet node configuration
        createNetworkContents(container, true);
        // mainnet node configuration
        createNetworkContents(container, false);

        initialize(); // initialize properties values

        return container;
    }

    private void createNetworkContents(Composite parent, boolean testnet) {

        Group group = new Group(parent, SWT.NONE);
        group.setText(testnet ? " Testnet node " : " Mainnet node ");
        GridLayout gl_grpTestnet = new GridLayout(2, false);
        gl_grpTestnet.marginTop = 5;
        gl_grpTestnet.marginRight = 5;
        gl_grpTestnet.marginLeft = 5;
        gl_grpTestnet.marginBottom = 5;
        group.setLayout(gl_grpTestnet);

        Label hostLabel = new Label(group, SWT.NONE);
        hostLabel.setText("Host");

        Text hostText = new Text(group, SWT.BORDER);
        hostText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblPort = new Label(group, SWT.NONE);
        lblPort.setText("Port");

        Spinner portSpinner = new Spinner(group, SWT.BORDER);
        portSpinner.setMaximum(65535);
        portSpinner.setMinimum(1);
        GridData gd_portSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_portSpinner.heightHint = 19;
        gd_portSpinner.widthHint = 163;
        portSpinner.setLayoutData(gd_portSpinner);

        Label lblHttpsButton = new Label(group, SWT.NONE);
        lblHttpsButton.setText("Https");

        Button httpsCheckbox = new Button(group, SWT.CHECK);
        httpsCheckbox.setToolTipText("Recommended if the server accepts SSL connections");

        Label usernameLabel = new Label(group, SWT.NONE);
        usernameLabel.setText("Username");

        Text usernameText = new Text(group, SWT.BORDER);
        usernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Label urlLabel = new Label(group, SWT.NONE);
        urlLabel.setText("URL");

        Text urlText = new Text(group, SWT.BORDER);
        urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        urlText.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                String text = urlText.getText();
                if (!text.startsWith("/"))
                    urlText.setText("/" + text);
            }

            @Override
            public void focusGained(FocusEvent e) {
            }

        });

        Label testnetPasswordLabel = new Label(group, SWT.NONE);
        testnetPasswordLabel.setText("Password");

        Text passwordText = new Text(group, SWT.BORDER | SWT.PASSWORD);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label timeoutLabel = new Label(group, SWT.NONE);
        timeoutLabel.setText("Timeout");

        Spinner timeoutSpinner = new Spinner(group, SWT.BORDER);
        timeoutSpinner.setMaximum(Integer.MAX_VALUE);
        timeoutSpinner.setMinimum(-1);
        timeoutSpinner.setToolTipText("Set -1 for undefined waiting time (not recommended)");
        GridData gd_timeoutSpinner = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_timeoutSpinner.heightHint = 19;
        gd_timeoutSpinner.widthHint = 163;
        timeoutSpinner.setLayoutData(gd_timeoutSpinner);

        // test button with feedbacks
        Composite compositeBtnTest = new Composite(group, SWT.NONE);
        compositeBtnTest.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        GridLayout gl_composite = new GridLayout(2, false);
        gl_composite.horizontalSpacing = 10;
        gl_composite.marginWidth = 0;
        compositeBtnTest.setLayout(gl_composite);
        Button btnTestConnection = new Button(compositeBtnTest, SWT.NONE);
        btnTestConnection.setText("Test");
        Composite compositeFeedbacks = new Composite(compositeBtnTest, SWT.NONE);
        Canvas networkFeedbackOK = new Canvas(compositeFeedbacks, SWT.NONE);
        networkFeedbackOK.setBounds(0, 0, 16, 16);
        networkFeedbackOK.setBackgroundImage(imageSuccess);
        networkFeedbackOK.setVisible(false);
        Canvas networkFeedbackERR = new Canvas(compositeFeedbacks, SWT.NONE);
        networkFeedbackERR.setBounds(0, 0, 16, 16);
        networkFeedbackERR.setBackgroundImage(imageError);
        networkFeedbackERR.setVisible(false);

        // error details
        Composite errorDetailsComposite = new Composite(group, SWT.BORDER);
        StyledText errorDetailsTextArea = new StyledText(errorDetailsComposite,
                SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
        GridData gd_errorDetailsTextArea = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_errorDetailsTextArea.grabExcessVerticalSpace = true;
        errorDetailsComposite.setLayoutData(gd_errorDetailsTextArea);
        errorDetailsComposite.setBackground(errorDetailsTextArea.getBackground());
        errorDetailsTextArea.setAlwaysShowScrollBars(false);
        errorDetailsComposite.addControlListener(new ControlListener() {

            private int TOP_MARGIN = 4;
            private int LEFT_MARGIN = 8;

            @Override
            public void controlResized(ControlEvent e) {
                Composite parent = errorDetailsTextArea.getParent();
                errorDetailsTextArea.setBounds(LEFT_MARGIN, TOP_MARGIN,
                        parent.getSize().x - 2 * parent.getBorderWidth() - LEFT_MARGIN,
                        parent.getSize().y - 2 * parent.getBorderWidth() - TOP_MARGIN);
            }

            @Override
            public void controlMoved(ControlEvent e) {
            }
        });

        btnTestConnection.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                errorDetailsTextArea.setText("");
                networkFeedbackOK.setVisible(false);
                networkFeedbackERR.setVisible(false);
            }

            @Override
            public void mouseUp(MouseEvent e) {
                String address = hostText.getText();
                Integer port = portSpinner.getSelection();
                String protocol = httpsCheckbox.getSelection() ? "https" : "http";
                String url = urlText.getText();
                String user = usernameText.getText();
                String password = passwordText.getText();
                Integer timeout = timeoutSpinner.getSelection();

                if (password.isEmpty()) {
                    try {
                        if (testnet)
                            password = secureStorage
                                    .node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE)
                                    .get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__TESTNET_PASSWORD, "");
                        else
                            password = secureStorage
                                    .node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE)
                                    .get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__MAINNET_PASSWORD, "");

                    } catch (StorageException e1) {
                        e1.printStackTrace();
                        errorDetailsTextArea.append("Error fetching the password from the secure store.\n\n");
                        errorDetailsTextArea.append(Throwables.getStackTraceAsString(e1));
                        networkFeedbackERR.setVisible(true);
                        return;
                    }
                }
                RPCBitcoinClient bitcoinClient = new RPCBitcoinClient(address, port, protocol, url, user, password,
                        timeout, TimeUnit.MILLISECONDS);

                try {
                    boolean isTestnet = bitcoinClient.isTestnet();

                    if (isTestnet != testnet) {
                        String expected = testnet ? "testnet" : "mainnet";
                        String actual = isTestnet ? "testnet" : "mainnet";
                        errorDetailsTextArea
                                .append("Wrong network type: expected " + expected + ", found " + actual + ".");
                        networkFeedbackERR.setVisible(true);
                        return;
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    errorDetailsTextArea.append("Cannot connect to the node.\n\n");
                    errorDetailsTextArea.append(Throwables.getStackTraceAsString(e1));
                    networkFeedbackERR.setVisible(true);
                    return;
                }
                errorDetailsTextArea.append("ok");
                networkFeedbackOK.setVisible(true);
            }
        });

        if (testnet)
            testnetHostText = hostText;
        else
            mainnetHostText = hostText;
        if (testnet)
            testnetPortSpinner = portSpinner;
        else
            mainnetPortSpinner = portSpinner;
        if (testnet)
            testnetHttpsCheckbox = httpsCheckbox;
        else
            mainnetHttpsCheckbox = httpsCheckbox;
        if (testnet)
            testnetUrlText = urlText;
        else
            mainnetUrlText = urlText;
        if (testnet)
            testnetUsernameText = usernameText;
        else
            mainnetUsernameText = usernameText;
        if (testnet)
            testnetPasswordText = passwordText;
        else
            mainnetPasswordText = passwordText;
        if (testnet)
            testnetTimeoutSpinner = timeoutSpinner;
        else
            mainnetTimeoutSpinner = timeoutSpinner;
    }

    private void initialize() {
        IPreferenceStore store = getPreferenceStore();

        testnetHostText.setText(store.getString(PreferenceConstants.P_TESTNET_HOST));
        testnetPortSpinner.setSelection(store.getInt(PreferenceConstants.P_TESTNET_PORT));
        testnetHttpsCheckbox.setSelection(store.getBoolean(PreferenceConstants.P_TESTNET_HTTPS));
        testnetUrlText.setText(store.getString(PreferenceConstants.P_TESTNET_URL));
        testnetUsernameText.setText(store.getString(PreferenceConstants.P_TESTNET_USERNAME));
        testnetTimeoutSpinner.setSelection(store.getInt(PreferenceConstants.P_TESTNET_TIMEOUT));

        if (isTestnetPasswordSaved())
            testnetPasswordText.setMessage("Not changed");
        else
            testnetPasswordText.setMessage("Enter a password");

        mainnetHostText.setText(store.getString(PreferenceConstants.P_MAINNET_HOST));
        mainnetPortSpinner.setSelection(store.getInt(PreferenceConstants.P_MAINNET_PORT));
        mainnetHttpsCheckbox.setSelection(store.getBoolean(PreferenceConstants.P_MAINNET_HTTPS));
        mainnetUrlText.setText(store.getString(PreferenceConstants.P_MAINNET_URL));
        mainnetUsernameText.setText(store.getString(PreferenceConstants.P_MAINNET_USERNAME));
        mainnetTimeoutSpinner.setSelection(store.getInt(PreferenceConstants.P_MAINNET_TIMEOUT));

        if (isMainnetPasswordSaved())
            mainnetPasswordText.setMessage("Not changed");
        else
            mainnetPasswordText.setMessage("Enter a password");

    }

    @Override
    public void dispose() {
        imageError.dispose();
        imageSuccess.dispose();
        super.dispose();
    }

    @Override
    public void performDefaults() {
        IPreferenceStore store = getPreferenceStore();
        testnetHostText.setText(store.getDefaultString(PreferenceConstants.P_TESTNET_HOST));
        testnetPortSpinner.setSelection(store.getDefaultInt(PreferenceConstants.P_TESTNET_PORT));
        testnetHttpsCheckbox.setSelection(store.getDefaultBoolean(PreferenceConstants.P_TESTNET_HTTPS));
        testnetUrlText.setText(store.getDefaultString(PreferenceConstants.P_TESTNET_URL));
        testnetUsernameText.setText(store.getDefaultString(PreferenceConstants.P_TESTNET_USERNAME));
        testnetTimeoutSpinner.setSelection(store.getDefaultInt(PreferenceConstants.P_TESTNET_TIMEOUT));
        secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE).removeNode();
        testnetPasswordText.setMessage("Enter a password");

        mainnetHostText.setText(store.getDefaultString(PreferenceConstants.P_MAINNET_HOST));
        mainnetPortSpinner.setSelection(store.getDefaultInt(PreferenceConstants.P_MAINNET_PORT));
        mainnetHttpsCheckbox.setSelection(store.getDefaultBoolean(PreferenceConstants.P_MAINNET_HTTPS));
        mainnetUrlText.setText(store.getDefaultString(PreferenceConstants.P_MAINNET_URL));
        mainnetUsernameText.setText(store.getDefaultString(PreferenceConstants.P_MAINNET_USERNAME));
        mainnetTimeoutSpinner.setSelection(store.getDefaultInt(PreferenceConstants.P_MAINNET_TIMEOUT));
        secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE).removeNode();
        mainnetPasswordText.setMessage("Enter a password");
        super.updateApplyButton();
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(PreferenceConstants.P_TESTNET_HOST, testnetHostText.getText());
        store.setValue(PreferenceConstants.P_TESTNET_PORT, testnetPortSpinner.getSelection());
        store.setValue(PreferenceConstants.P_TESTNET_HTTPS, testnetHttpsCheckbox.getSelection());
        store.setValue(PreferenceConstants.P_TESTNET_URL, testnetUrlText.getText());
        store.setValue(PreferenceConstants.P_TESTNET_USERNAME, testnetUsernameText.getText());
        store.setValue(PreferenceConstants.P_TESTNET_TIMEOUT, testnetTimeoutSpinner.getSelection());

        if (!testnetPasswordText.getText().isEmpty()) {
            try {
                secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE).put(
                        SecureStorageUtils.SECURE_STORAGE__PROPERTY__TESTNET_PASSWORD, testnetPasswordText.getText(),
                        true);
            } catch (StorageException e) {
                e.printStackTrace();
                return false;
            }
        }

        store.setValue(PreferenceConstants.P_MAINNET_HOST, mainnetHostText.getText());
        store.setValue(PreferenceConstants.P_MAINNET_PORT, mainnetPortSpinner.getSelection());
        store.setValue(PreferenceConstants.P_MAINNET_HTTPS, mainnetHttpsCheckbox.getSelection());
        store.setValue(PreferenceConstants.P_MAINNET_URL, mainnetUrlText.getText());
        store.setValue(PreferenceConstants.P_MAINNET_USERNAME, mainnetUsernameText.getText());
        store.setValue(PreferenceConstants.P_MAINNET_TIMEOUT, mainnetTimeoutSpinner.getSelection());

        if (!mainnetPasswordText.getText().isEmpty()) {
            try {
                secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE).put(
                        SecureStorageUtils.SECURE_STORAGE__PROPERTY__MAINNET_PASSWORD, mainnetPasswordText.getText(),
                        true);
            } catch (StorageException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            setBitcoinClientFactoryNodes(store);
        } catch (StorageException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static void setBitcoinClientFactoryNodes(IPreferenceStore store) throws StorageException {
        ISecurePreferences secureStore = SecurePreferencesFactory.getDefault();

        BitcoinClient testnetClient = getTestnetBitcoinClient(store, secureStore);
        BitcoinClient mainnetClient = getMainnetBitcoinClient(store, secureStore);

        Injector injector = BalzacActivator.getInstance().getInjector(BalzacActivator.XYZ_BALZACLANG_BALZAC);
        BitcoinClientFactory factory = injector.getInstance(BitcoinClientFactory.class);
        factory.setMainnetClient(mainnetClient);
        factory.setTestnetClient(testnetClient);
    }

    private static BitcoinClient getMainnetBitcoinClient(IPreferenceStore store, ISecurePreferences secureStore)  throws StorageException {
        String mainnetHost = store.getString(PreferenceConstants.P_MAINNET_HOST);
        int mainnetPort = store.getInt(PreferenceConstants.P_MAINNET_PORT);
        String mainnetProtocol = store.getBoolean(PreferenceConstants.P_MAINNET_HTTPS) ? "https" : "http";
        String mainnetUrl = store.getString(PreferenceConstants.P_MAINNET_URL);
        String mainnetUsername = store.getString(PreferenceConstants.P_MAINNET_USERNAME);
        int mainnetTimeout = store.getInt(PreferenceConstants.P_MAINNET_TIMEOUT);
        String mainnetPassword = secureStore.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE)
                .get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__MAINNET_PASSWORD, "");

        BitcoinClient mainnetClient = new RPCBitcoinClient(mainnetHost, mainnetPort, mainnetProtocol, mainnetUrl,
                mainnetUsername, mainnetPassword, mainnetTimeout, TimeUnit.MILLISECONDS);

        return mainnetClient;
    }

    private static BitcoinClient getTestnetBitcoinClient(IPreferenceStore store, ISecurePreferences secureStore)  throws StorageException {
        String testnetHost = store.getString(PreferenceConstants.P_TESTNET_HOST);
        int testnetPort = store.getInt(PreferenceConstants.P_TESTNET_PORT);
        String testnetProtocol = store.getBoolean(PreferenceConstants.P_TESTNET_HTTPS) ? "https" : "http";
        String testnetUrl = store.getString(PreferenceConstants.P_TESTNET_URL);
        String testnetUsername = store.getString(PreferenceConstants.P_TESTNET_USERNAME);
        int testnetTimeout = store.getInt(PreferenceConstants.P_TESTNET_TIMEOUT);
        String testnetPassword = secureStore.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE)
                .get(SecureStorageUtils.SECURE_STORAGE__PROPERTY__TESTNET_PASSWORD, "");

        BitcoinClient testnetClient = new RPCBitcoinClient(testnetHost, testnetPort, testnetProtocol, testnetUrl,
                testnetUsername, testnetPassword, testnetTimeout, TimeUnit.MILLISECONDS);

        return testnetClient;
    }

    private boolean isTestnetPasswordSaved() {
        ISecurePreferences node = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE);
        return Arrays.asList(node.keys()).contains(SecureStorageUtils.SECURE_STORAGE__PROPERTY__TESTNET_PASSWORD);
    }

    private boolean isMainnetPasswordSaved() {
        ISecurePreferences node = secureStorage.node(SecureStorageUtils.SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE);
        return Arrays.asList(node.keys()).contains(SecureStorageUtils.SECURE_STORAGE__PROPERTY__MAINNET_PASSWORD);
    }

}

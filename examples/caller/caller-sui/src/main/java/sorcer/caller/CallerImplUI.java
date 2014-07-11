package sorcer.caller;

import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.lookup.LookupCache;
import net.jini.lookup.ServiceDiscoveryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sorcer.core.SorcerConstants;
import sorcer.service.Accessor;
import sorcer.service.Context;
import sorcer.service.ContextException;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CallerImplUI extends JPanel {

	private final static Logger logger = LoggerFactory.getLogger(CallerImplUI.class);

    private Caller caller;
    private JButton refresh;
    Choice choice = null;
    JTextArea outputTA = new JTextArea(30, 30);
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextField cmdTF = new JTextField("No command is being executed");
    JScrollPane scroller;
    ServiceID sID;
    private Map<Integer, Context> choiceCtxs = new HashMap<Integer, Context>();

    private javax.swing.Timer autoRefresh;
    private boolean keepAlive=true;
    private transient Thread thread;

    Context currentCtx = null;
    ArrayList<Context> curCtxts = new ArrayList<Context>();

    public CallerImplUI(final Object arg) {
        super();
        getAccessibleContext().setAccessibleName("Console Viewer");
        this.setSize(500, 650);
        final ServiceItem item = (ServiceItem) arg;
        try {
            caller = ((Caller) item.service);
            sID = item.serviceID;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createUI(item);
            }
        });
    }

    private void createUI(ServiceItem item) {

        JPanel qPanel = new JPanel();
        qPanel.setLayout(new BoxLayout(qPanel, BoxLayout.X_AXIS));
        qPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(),
                                ""),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        qPanel.add(Box.createHorizontalStrut(8));
        choice=new Choice();
        qPanel.add(new JLabel("Choose the command to view its output"));
        qPanel.add(choice);
        choice.add("- no command run -");
        choice.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                refresh_contexts();
            }

            public void focusLost(FocusEvent e) {
                refresh_contexts();
            }

        });

        choice.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (!choiceCtxs.isEmpty()) currentCtx = choiceCtxs.get(new Integer(choice.getSelectedIndex()));
                refresh();
            }
        });

        qPanel.add(Box.createHorizontalStrut(8));
        JPanel cPanel = new JPanel();
        cPanel.setLayout(new BoxLayout(cPanel, BoxLayout.X_AXIS));
        cPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEtchedBorder(),
                                "Console Viewer"),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        cPanel.add(Box.createHorizontalStrut(8));
        cPanel.add(new JLabel("Currently executed command: "));
        cPanel.add(cmdTF);


        outputTA.setLineWrap(true);
        //outputTA.setAutoscrolls(true);

        JPanel cq = new JPanel();
        cq.setLayout(new BorderLayout());

        cq.add(qPanel, BorderLayout.NORTH);

        cq.add(cPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        JPanel entryPanel = new JPanel(new BorderLayout());
        entryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel lbl = new JLabel(
                "Refresh console output");
        entryPanel.add(lbl, BorderLayout.NORTH);
        refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                refresh();
            }
        });
        entryPanel.add(refresh, BorderLayout.SOUTH);

        scroller = new JScrollPane(outputTA);
        scroller.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        add(cq, BorderLayout.NORTH);
        add(entryPanel, BorderLayout.CENTER);
        add(scroller, BorderLayout.SOUTH);

        autoRefresh =
                new javax.swing.Timer(
                        2*1000, new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        refresh();
                    }
                });

    }

    /**
     * Override to ensure the thread that is created is interrupted
     */

    protected void showInformation(String text) {
        JOptionPane.showMessageDialog(this, text,
                "Information Message",
                JOptionPane.INFORMATION_MESSAGE);
    }

    protected void showError(String text) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, text,
                "System Error", JOptionPane.ERROR_MESSAGE);
    }

    public void refresh() {
        Caller qc = Accessor.getService(Caller.class);
        if (qc!=null) {
            try {
                String curOut = null;
                if (currentCtx!=null) {
                    String errorStr = qc.getCurrentError(currentCtx);
                    curOut = qc.getCurrentOutput(currentCtx);
                    if (curOut==null || curOut=="") {
                        curOut = CallerUtil.getCallOutput(currentCtx);
                    }
                    String cmdStr = (CallerUtil.getCmds(currentCtx))[0];
                    String[] argss = CallerUtil.getArgs(currentCtx);
                    for (int i=0;i<argss.length;i++) {
                        cmdStr = cmdStr + " " + argss[i];
                    }
                    cmdTF.setText(cmdStr);
                    StringBuilder sb = new StringBuilder((errorStr!=null ? errorStr : ""));
                    sb.append(curOut!=null ? curOut : "");
                    outputTA.setText(sb.toString());
                } else {
                    cmdTF.setText("No command running");
                }
                //scroller.repaint();
            } catch (ContextException ce) {
                ce.printStackTrace();
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }


    }

    public void refresh_contexts() {
        Caller qc = Accessor.getService(Caller.class);
        if (qc!=null) {
            try {
                List<Context> curCtxts = qc.getCurrentContexts();
                Iterator ctxIt = curCtxts.iterator();
                choice.removeAll();
                choiceCtxs = new HashMap<Integer, Context>();
                int choicePos = 0;
                while (ctxIt.hasNext()) {
                    Context ctx = (Context)ctxIt.next();
                    if (ctx!=null) {
                        System.out.println("Context is: " + ctx);
                        String cmdStr = (CallerUtil.getCmds(ctx))[0];
                        String[] argss = CallerUtil.getArgs(ctx);
                        for (int i=0;i<argss.length;i++) {
                            cmdStr = cmdStr + " " + argss[i];
                        }
                        choiceCtxs.put(new Integer(choicePos), ctx);

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        if (qc.getCurrentStatus(ctx).booleanValue())
                            cmdStr = "RUNNING since " + sdf.format(CallerUtil.getStarted(ctx)) + ": " + cmdStr;
                        else
                            cmdStr = "FINISHED @ " + sdf.format(CallerUtil.getStopped(ctx)) + ": " + cmdStr;
                        choice.add(cmdStr);
                        choicePos++;
                    }
                }
                //scroller.repaint();
            } catch (ContextException ce) {
                ce.printStackTrace();
            } catch (RemoteException re) {
                re.printStackTrace();
            }
        }


    }
}

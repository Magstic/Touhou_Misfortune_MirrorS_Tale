import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

public final class CcDatCropViewer {
    private static final int ZOOM = 3;
    
    private static final class Entry {
        final int spriteId;
        final int[] row;

        Entry(int spriteId, int[] row) {
            this.spriteId = spriteId;
            this.row = row;
        }

        int imgIndex() {
            return row[0];
        }

        int srcX() {
            return row[1];
        }

        int srcY() {
            return row[2];
        }

        int w() {
            return row[3];
        }

        int h() {
            return row[4];
        }

        int ax() {
            return row[5];
        }

        int ay() {
            return row[6];
        }

        int len() {
            return row.length;
        }
    }

    private static final class EntryTableModel extends AbstractTableModel {
        private final List<Entry> entries = new ArrayList<Entry>();

        void setEntries(List<Entry> es) {
            entries.clear();
            entries.addAll(es);
            fireTableDataChanged();
        }

        Entry get(int row) {
            if (row < 0 || row >= entries.size()) {
                return null;
            }
            return entries.get(row);
        }

        public int getRowCount() {
            return entries.size();
        }

        public int getColumnCount() {
            return 9;
        }

        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "spriteId";
                case 1:
                    return "img";
                case 2:
                    return "x";
                case 3:
                    return "y";
                case 4:
                    return "w";
                case 5:
                    return "h";
                case 6:
                    return "ax";
                case 7:
                    return "ay";
                case 8:
                    return "len";
                default:
                    return "";
            }
        }

        public Object getValueAt(int r, int c) {
            Entry e = entries.get(r);
            switch (c) {
                case 0:
                    return Integer.valueOf(e.spriteId);
                case 1:
                    return Integer.valueOf(e.imgIndex());
                case 2:
                    return Integer.valueOf(e.srcX());
                case 3:
                    return Integer.valueOf(e.srcY());
                case 4:
                    return Integer.valueOf(e.w());
                case 5:
                    return Integer.valueOf(e.h());
                case 6:
                    return Integer.valueOf(e.ax());
                case 7:
                    return Integer.valueOf(e.ay());
                case 8:
                    return Integer.valueOf(e.len());
                default:
                    return "";
            }
        }
    }

    private static final class AtlasPreviewPanel extends JPanel {
        private static final int PAD = 20;
        private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 10);
        private static final BasicStroke NORMAL_STROKE = new BasicStroke(1f);
        private static final BasicStroke HIGHLIGHT_STROKE = new BasicStroke(2.5f);

        private BufferedImage atlas;
        private List<Entry> entries = new ArrayList<Entry>();
        private int selectedSpriteId = -1;
        private int zoom = ZOOM;

        AtlasPreviewPanel() {
            setBackground(Color.DARK_GRAY);
        }

        void setAtlas(BufferedImage atlas, List<Entry> entries, int zoom) {
            this.atlas = atlas;
            this.entries = (entries != null) ? entries : new ArrayList<Entry>();
            this.selectedSpriteId = -1;
            this.zoom = zoom;
            updatePreferredSize();
            repaint();
        }

        void setSelectedSpriteId(int spriteId) {
            this.selectedSpriteId = spriteId;
            repaint();
        }

        Rectangle getSelectedRect() {
            if (atlas == null) {
                return null;
            }
            for (int i = 0; i < entries.size(); i++) {
                Entry e = entries.get(i);
                if (e.spriteId == selectedSpriteId) {
                    return new Rectangle(
                            PAD + e.srcX() * zoom,
                            PAD + e.srcY() * zoom,
                            e.w() * zoom,
                            e.h() * zoom);
                }
            }
            return null;
        }

        private void updatePreferredSize() {
            if (atlas != null) {
                int w = atlas.getWidth() * zoom + PAD * 2;
                int h = atlas.getHeight() * zoom + PAD * 2;
                setPreferredSize(new Dimension(w, h));
            } else {
                setPreferredSize(new Dimension(520, 520));
            }
            revalidate();
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            if (atlas == null) {
                g.setColor(Color.WHITE);
                g.drawString("Enter imgIndex and click Apply to load atlas.", 12, 20);
                return;
            }

            // Draw atlas image
            int aw = atlas.getWidth() * zoom;
            int ah = atlas.getHeight() * zoom;
            g.drawImage(atlas, PAD, PAD, aw, ah, null);

            // Atlas border
            g.setColor(new Color(80, 80, 80));
            g.drawRect(PAD - 1, PAD - 1, aw + 1, ah + 1);

            g.setFont(LABEL_FONT);
            FontMetrics fm = g.getFontMetrics();

            // Draw crop rects: non-selected first, selected last (on top)
            Entry selectedEntry = null;
            for (int i = 0; i < entries.size(); i++) {
                Entry e = entries.get(i);
                if (e.spriteId == selectedSpriteId) {
                    selectedEntry = e;
                    continue;
                }
                drawEntry(g, fm, e, false);
            }
            if (selectedEntry != null) {
                drawEntry(g, fm, selectedEntry, true);
            }
        }

        private void drawEntry(Graphics2D g, FontMetrics fm, Entry e, boolean selected) {
            int rx = PAD + e.srcX() * zoom;
            int ry = PAD + e.srcY() * zoom;
            int rw = e.w() * zoom;
            int rh = e.h() * zoom;

            if (selected) {
                g.setStroke(HIGHLIGHT_STROKE);
                g.setColor(new Color(255, 255, 0, 230));
            } else {
                g.setStroke(NORMAL_STROKE);
                g.setColor(new Color(0, 255, 255, 120));
            }
            g.drawRect(rx, ry, rw - 1, rh - 1);
            g.setStroke(NORMAL_STROKE);

            // SpriteId label
            String label = String.valueOf(e.spriteId);
            int tw = fm.stringWidth(label);
            int th = fm.getHeight();

            if (rw >= tw + 4 && rh >= th + 2) {
                int lx = rx + 2;
                int ly = ry + th - 1;
                g.setColor(new Color(0, 0, 0, 160));
                g.fillRect(lx - 1, ly - th + 3, tw + 2, th - 1);
                g.setColor(selected ? Color.YELLOW : new Color(220, 220, 220));
                g.drawString(label, lx, ly);
            }

            // Anchor crosshair for selected entry
            if (selected) {
                int cx = PAD + (e.srcX() + e.ax()) * zoom;
                int cy = PAD + (e.srcY() + e.ay()) * zoom;
                g.setColor(new Color(255, 80, 80, 200));
                g.drawLine(cx - 4, cy, cx + 4, cy);
                g.drawLine(cx, cy - 4, cx, cy + 4);
            }
        }
    }

    private final JFrame frame;
    private final JTextField ccPath;
    private final JTextField spDir;
    private final JTextField imgIndexFilter;
    private final EntryTableModel model;
    private final JTable table;
    private final AtlasPreviewPanel atlasPreview;
    private final JScrollPane atlasScroll;

    private int[][] cc;
    private BufferedImage cachedAtlas;
    private int cachedAtlasImgIndex = -1;

    public CcDatCropViewer() {
        frame = new JFrame("cc.dat Crop Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ccPath = new JTextField(20);
        spDir = new JTextField(20);
        imgIndexFilter = new JTextField(5);

        File projectRoot = guessProjectRoot();
        ccPath.setText(new File(projectRoot, "res/cc.dat").getAbsolutePath());
        spDir.setText(new File(projectRoot, "res/sp").getAbsolutePath());

        JButton browseCc = new JButton("...");
        browseCc.setToolTipText("Browse cc.dat");
        browseCc.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                ccPath.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JButton browseSp = new JButton("...");
        browseSp.setToolTipText("Browse sp/ directory");
        browseSp.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                spDir.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });

        JButton load = new JButton("Load");
        load.addActionListener(e -> onLoad());

        JButton apply = new JButton("Apply");
        apply.addActionListener(e -> refreshTable());
        imgIndexFilter.addActionListener(e -> refreshTable());

        // -- Top panel: 2-row GridBagLayout --
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 3, 2, 3);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridy = 0;
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        top.add(new JLabel("cc.dat:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        top.add(ccPath, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        top.add(browseCc, gbc);
        gbc.gridx = 3;
        top.add(new JLabel("sp/:"), gbc);
        gbc.gridx = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        top.add(spDir, gbc);
        gbc.gridx = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        top.add(browseSp, gbc);
        gbc.gridx = 6;
        top.add(load, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        top.add(new JLabel("imgIndex:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        top.add(imgIndexFilter, gbc);
        gbc.gridx = 2;
        top.add(apply, gbc);

        model = new EntryTableModel();
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        atlasPreview = new AtlasPreviewPanel();
        atlasScroll = new JScrollPane(atlasPreview);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    onSelection();
                }
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(table), atlasScroll);
        split.setResizeWeight(0.35);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(split, BorderLayout.CENTER);

        frame.setSize(1100, 700);
        frame.setLocationRelativeTo(null);
    }

    private void onLoad() {
        try {
            cc = loadCcDat(new File(ccPath.getText().trim()));
            cachedAtlas = null;
            cachedAtlasImgIndex = -1;
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.toString(), "Load Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        // Auto-load cc.dat if not yet loaded
        if (cc == null) {
            try {
                cc = loadCcDat(new File(ccPath.getText().trim()));
                cachedAtlas = null;
                cachedAtlasImgIndex = -1;
            } catch (Exception ex) {
                model.setEntries(new ArrayList<Entry>());
                atlasPreview.setAtlas(null, null, ZOOM);
                JOptionPane.showMessageDialog(frame, ex.toString(), "Load Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Integer imgFilter = parseNullableInt(imgIndexFilter.getText().trim());

        List<Entry> list = new ArrayList<Entry>();
        for (int i = 0; i < cc.length; i++) {
            int[] row = cc[i];
            if (row == null || row.length < 7) {
                continue;
            }
            if (imgFilter != null && row[0] != imgFilter.intValue()) {
                continue;
            }
            list.add(new Entry(i, row));
        }

        model.setEntries(list);

        // Load atlas for the filtered imgIndex
        if (imgFilter != null) {
            loadAtlasForIndex(imgFilter.intValue(), list);
        } else {
            atlasPreview.setAtlas(null, null, ZOOM);
        }

        if (list.size() > 0) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    private void loadAtlasForIndex(int imgIndex, List<Entry> entries) {
        if (imgIndex == cachedAtlasImgIndex && cachedAtlas != null) {
            atlasPreview.setAtlas(cachedAtlas, entries, ZOOM);
            return;
        }

        File sp = new File(spDir.getText().trim());
        String fileName = resolveAtlasFile(sp, imgIndex);
        if (fileName == null) {
            cachedAtlas = null;
            cachedAtlasImgIndex = -1;
            atlasPreview.setAtlas(null, entries, ZOOM);
            return;
        }

        try {
            cachedAtlas = ImageIO.read(new File(sp, fileName));
            cachedAtlasImgIndex = imgIndex;
        } catch (IOException ex) {
            cachedAtlas = null;
            cachedAtlasImgIndex = -1;
        }
        atlasPreview.setAtlas(cachedAtlas, entries, ZOOM);
    }

    private void onSelection() {
        Entry e = model.get(table.getSelectedRow());
        if (e == null) {
            atlasPreview.setSelectedSpriteId(-1);
            return;
        }

        atlasPreview.setSelectedSpriteId(e.spriteId);

        // Scroll atlas view to show the selected crop rect
        Rectangle r = atlasPreview.getSelectedRect();
        if (r != null) {
            r.grow(30, 30);
            atlasPreview.scrollRectToVisible(r);
        }
    }

    private static String resolveAtlasFile(File spDir, int imgIndex) {
        if (spDir == null || !spDir.isDirectory()) {
            return null;
        }

        // Try simple NNN.gif format (e.g. 010.gif)
        String simple = pad3(imgIndex) + ".gif";
        if (new File(spDir, simple).isFile()) {
            return simple;
        }

        // Fallback: scan for NNNN_*.gif patterns (legacy extracted names)
        String p1 = pad4(imgIndex + 1) + "_";
        String p0 = pad4(imgIndex) + "_";

        File[] files = spDir.listFiles();
        if (files == null) {
            return null;
        }

        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            if (n.endsWith(".gif") && n.startsWith(p1)) {
                return n;
            }
        }

        for (int i = 0; i < files.length; i++) {
            String n = files[i].getName();
            if (n.endsWith(".gif") && n.startsWith(p0)) {
                return n;
            }
        }

        return null;
    }

    private static String pad3(int n) {
        if (n < 10) {
            return "00" + n;
        }
        if (n < 100) {
            return "0" + n;
        }
        return String.valueOf(n);
    }

    private static int[][] loadCcDat(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("Missing file: " + file);
        }

        byte[] data;
        FileInputStream in = new FileInputStream(file);
        try {
            data = readAll(in);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }

        if (data.length < 2) {
            throw new IOException("Invalid cc.dat");
        }

        int count = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        int n = 2;

        int[][] cc = new int[count][];
        int[] lens = new int[count];

        for (int i = 0; i < count; i++) {
            if (n >= data.length) {
                throw new IOException("Invalid cc.dat (length table)");
            }
            int len = data[n] & 0xFF;
            n++;
            lens[i] = len;
            cc[i] = new int[len];
        }

        for (int j = 0; j < count; j++) {
            for (int k = 0; k < lens[j]; k++) {
                if (n + 1 >= data.length) {
                    throw new IOException("Invalid cc.dat (payload)");
                }
                int v = ((data[n] & 0xFF) << 8) | (data[n + 1] & 0xFF);
                n += 2;
                cc[j][k] = v;
            }
        }

        return cc;
    }

    private static byte[] readAll(FileInputStream in) throws IOException {
        byte[] buf = new byte[4096];
        int r;
        List<byte[]> chunks = new ArrayList<byte[]>();
        int total = 0;
        while ((r = in.read(buf)) != -1) {
            byte[] c = new byte[r];
            System.arraycopy(buf, 0, c, 0, r);
            chunks.add(c);
            total += r;
        }
        byte[] out = new byte[total];
        int off = 0;
        for (int i = 0; i < chunks.size(); i++) {
            byte[] c = chunks.get(i);
            System.arraycopy(c, 0, out, off, c.length);
            off += c.length;
        }
        return out;
    }

    private static Integer parseNullableInt(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }
        try {
            return Integer.valueOf(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String pad4(int n) {
        if (n < 10) {
            return "000" + n;
        }
        if (n < 100) {
            return "00" + n;
        }
        if (n < 1000) {
            return "0" + n;
        }
        return String.valueOf(n);
    }

    public void show() {
        frame.setVisible(true);
    }

    private static File guessProjectRoot() {
        // Try to locate project root from CWD or known markers
        File cwd = new File(System.getProperty("user.dir", "."));
        // If CWD itself has res/cc.dat, use it
        if (new File(cwd, "res/cc.dat").isFile()) {
            return cwd;
        }
        // If CWD is tools/, go up one level
        File parent = cwd.getParentFile();
        if (parent != null && new File(parent, "res/cc.dat").isFile()) {
            return parent;
        }
        // Fallback: try to resolve from class source location
        try {
            java.net.URL url = CcDatCropViewer.class.getProtectionDomain().getCodeSource().getLocation();
            File classDir = new File(url.toURI());
            if (classDir.isFile()) {
                classDir = classDir.getParentFile();
            }
            // classDir might be build/tools-classes or tools/
            for (int i = 0; i < 3; i++) {
                if (classDir == null) break;
                if (new File(classDir, "res/cc.dat").isFile()) {
                    return classDir;
                }
                classDir = classDir.getParentFile();
            }
        } catch (Exception ignore) {
        }
        return cwd;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CcDatCropViewer().show());
    }
}
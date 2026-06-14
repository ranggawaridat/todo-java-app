package todolist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class DataTasks extends javax.swing.JFrame {

    Connection conn;
    int selectedTaskId = -1;

    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(DataTasks.class.getName());

    // =========================================================
    // WARNA PALETTE (Minimalis Putih Profesional)
    // =========================================================
    private static final Color BG_WHITE      = new Color(0xFFFFFF);
    private static final Color BG_SURFACE    = new Color(0xF8F8F8);
    private static final Color BG_DARK_BTN   = new Color(0x1A1A1A);
    private static final Color BORDER_COLOR  = new Color(0xE0E0E0);
    private static final Color TEXT_PRIMARY  = new Color(0x1A1A1A);
    private static final Color TEXT_MUTED    = new Color(0x888888);

    // Status badge colors
    private static final Color CLR_PENDING_BG  = new Color(0xFEF3C7);
    private static final Color CLR_PENDING_FG  = new Color(0x92400E);
    private static final Color CLR_DONE_BG     = new Color(0xD1FAE5);
    private static final Color CLR_DONE_FG     = new Color(0x065F46);

    // Countdown colors
    private static final Color CLR_GREEN   = new Color(0x059669);
    private static final Color CLR_YELLOW  = new Color(0xD97706);
    private static final Color CLR_RED     = new Color(0xDC2626);

    // Kategori badge colors
    private static final Color[][] KATEGORI_COLORS = {
        // { BG, FG }
        {new Color(0xEDE9FE), new Color(0x5B21B6)}, // Kuliah - ungu
        {new Color(0xDBEAFE), new Color(0x1E40AF)}, // Project - biru
        {new Color(0xF3F4F6), new Color(0x374151)}, // Pribadi - abu
    };
    private static final String[] KATEGORI_LIST = {"Kuliah", "Project", "Pribadi"};

    // =========================================================
    // LOAD DATA
    // =========================================================
    public void loadData() {
        try {
            String filterKategori = (String) cbFilterKategori.getSelectedItem();
            String sql;
            PreparedStatement ps;

            if (filterKategori == null || filterKategori.equals("Semua")) {
                sql = "SELECT * FROM tasks ORDER BY deadline ASC, created_at DESC";
                ps = conn.prepareStatement(sql);
            } else {
                sql = "SELECT * FROM tasks WHERE kategori=? ORDER BY deadline ASC, created_at DESC";
                ps = conn.prepareStatement(sql);
                ps.setString(1, filterKategori);
            }

            ResultSet rs = ps.executeQuery();
            DefaultTableModel model = (DefaultTableModel) tableTasks.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                java.sql.Date sqlDeadline = rs.getDate("deadline");
                String countdownText = hitungCountdown(sqlDeadline);

                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("kategori"),
                    rs.getString("status"),
                    sqlDeadline != null ? sqlDeadline.toString() : "-",
                    countdownText
                });
            }

            tableTasks.setDefaultRenderer(Object.class, new StyledCellRenderer());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // HITUNG COUNTDOWN
    // =========================================================
    private String hitungCountdown(java.sql.Date sqlDeadline) {
        if (sqlDeadline == null) return "-";
        LocalDate deadline = sqlDeadline.toLocalDate();
        LocalDate today = LocalDate.now();
        long sisaHari = ChronoUnit.DAYS.between(today, deadline);

        if (sisaHari < 0)       return "Terlambat " + Math.abs(sisaHari) + " hari!";
        else if (sisaHari == 0) return "Hari ini!";
        else if (sisaHari == 1) return "Besok!";
        else                    return "Sisa " + sisaHari + " hari";
    }

    // =========================================================
    // CELL RENDERER: Warna baris & badge
    // =========================================================
    class StyledCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String countdown = table.getValueAt(row, 5).toString();
            String status    = table.getValueAt(row, 3).toString();
            String kategori  = table.getValueAt(row, 2) != null ? table.getValueAt(row, 2).toString() : "";

            JLabel label = (JLabel) c;
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            if (!isSelected) {
                // Warna baris berdasarkan countdown
                if (countdown.contains("Terlambat")) {
                    c.setBackground(new Color(0xFFF5F5));
                } else if (countdown.contains("Hari ini") || countdown.contains("Besok")) {
                    c.setBackground(new Color(0xFFFBEB));
                } else {
                    c.setBackground(BG_WHITE);
                }
                c.setForeground(TEXT_PRIMARY);
            } else {
                c.setBackground(new Color(0xF0F0F0));
                c.setForeground(TEXT_PRIMARY);
            }

            // Kolom Status: tampilkan badge
            if (column == 3) {
                if ("done".equalsIgnoreCase(status)) {
                    label.setForeground(CLR_DONE_FG);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    label.setText("● Selesai");
                } else {
                    label.setForeground(CLR_PENDING_FG);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    label.setText("○ Pending");
                }
            }

            // Kolom Kategori: warna per kategori
            if (column == 2) {
                int idx = getKategoriIndex(kategori);
                if (idx >= 0 && !isSelected) {
                    label.setForeground(KATEGORI_COLORS[idx][1]);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
            }

            // Kolom Countdown: warna
            if (column == 5) {
                if (!isSelected) {
                    if (countdown.contains("Terlambat")) label.setForeground(CLR_RED);
                    else if (countdown.contains("Hari ini") || countdown.contains("Besok")) label.setForeground(CLR_YELLOW);
                    else if (countdown.contains("Sisa"))    label.setForeground(CLR_GREEN);
                    else                                    label.setForeground(TEXT_MUTED);
                }
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
            }

            return c;
        }

        private int getKategoriIndex(String kat) {
            for (int i = 0; i < KATEGORI_LIST.length; i++) {
                if (KATEGORI_LIST[i].equalsIgnoreCase(kat)) return i;
            }
            return -1;
        }
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public DataTasks() {
        initComponents();
        styleComponents();

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Task", "Kategori", "Status", "Deadline", "Countdown"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableTasks.setModel(model);

        // Sembunyikan kolom ID
        tableTasks.getColumnModel().getColumn(0).setMinWidth(0);
        tableTasks.getColumnModel().getColumn(0).setMaxWidth(0);
        tableTasks.getColumnModel().getColumn(0).setWidth(0);

        // Lebar kolom
        tableTasks.getColumnModel().getColumn(1).setPreferredWidth(160);
        tableTasks.getColumnModel().getColumn(2).setPreferredWidth(70);
        tableTasks.getColumnModel().getColumn(3).setPreferredWidth(65);
        tableTasks.getColumnModel().getColumn(4).setPreferredWidth(85);
        tableTasks.getColumnModel().getColumn(5).setPreferredWidth(110);

        conn = DBConnection.connect();
        loadData();
    }

    // =========================================================
    // STYLING (dipanggil setelah initComponents)
    // =========================================================
    private void styleComponents() {
        // Window
        getContentPane().setBackground(BG_WHITE);

        // Header label
        lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblJudul.setForeground(TEXT_PRIMARY);

        // Panel input
        panelInput.setBackground(BG_SURFACE);
        panelInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Labels kecil
        for (JLabel lbl : new JLabel[]{lblTask, lblKategori, lblDeadline}) {
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lbl.setForeground(TEXT_MUTED);
        }
        lblDeadlineHint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblDeadlineHint.setForeground(TEXT_MUTED);

        // Input fields
        for (JTextField tf : new JTextField[]{txtTask, txtDeadline}) {
            tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tf.setForeground(TEXT_PRIMARY);
            tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            tf.setBackground(BG_WHITE);
        }

        // ComboBox
        cbKategori.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbKategori.setBackground(BG_WHITE);
        cbFilterKategori.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cbFilterKategori.setBackground(BG_WHITE);

        // Tombol Tambah (dark)
        btnTambah.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTambah.setBackground(BG_DARK_BTN);
        btnTambah.setForeground(Color.WHITE);
        btnTambah.setFocusPainted(false);
        btnTambah.setBorderPainted(false);
        btnTambah.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tombol Selesai (hijau soft)
        btnSelesai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSelesai.setBackground(CLR_DONE_BG);
        btnSelesai.setForeground(CLR_DONE_FG);
        btnSelesai.setBorder(BorderFactory.createLineBorder(new Color(0xA7F3D0), 1));
        btnSelesai.setFocusPainted(false);
        btnSelesai.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tombol Hapus (merah soft)
        btnHapus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapus.setBackground(new Color(0xFFF5F5));
        btnHapus.setForeground(new Color(0x991B1B));
        btnHapus.setBorder(BorderFactory.createLineBorder(new Color(0xFECACA), 1));
        btnHapus.setFocusPainted(false);
        btnHapus.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tombol Filter
        btnFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnFilter.setBackground(BG_WHITE);
        btnFilter.setForeground(TEXT_PRIMARY);
        btnFilter.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        btnFilter.setFocusPainted(false);
        btnFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tabel
        tableTasks.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableTasks.setRowHeight(36);
        tableTasks.setShowVerticalLines(false);
        tableTasks.setShowHorizontalLines(true);
        tableTasks.setGridColor(new Color(0xF0F0F0));
        tableTasks.setSelectionBackground(new Color(0xF0F0F0));
        tableTasks.setSelectionForeground(TEXT_PRIMARY);
        tableTasks.setIntercellSpacing(new Dimension(0, 0));
        tableTasks.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tableTasks.getTableHeader().setBackground(BG_SURFACE);
        tableTasks.getTableHeader().setForeground(TEXT_MUTED);
        tableTasks.getTableHeader().setBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        // ScrollPane
        jScrollPane1.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));

        // Label filter
        lblFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFilter.setForeground(TEXT_MUTED);
    }

    // =========================================================
    // INIT COMPONENTS
    // =========================================================
    @SuppressWarnings("unchecked")
    private void initComponents() {
        lblJudul         = new JLabel("Todo List");
        panelInput       = new JPanel();
        lblTask          = new JLabel("TASK");
        txtTask          = new JTextField();
        lblKategori      = new JLabel("KATEGORI");
        cbKategori       = new JComboBox<>(KATEGORI_LIST);
        btnTambah        = new JButton("+ Tambah");
        lblDeadline      = new JLabel("DEADLINE");
        txtDeadline      = new JTextField();
        lblDeadlineHint  = new JLabel("contoh: 2026-06-30");
        lblFilter        = new JLabel("Filter:");
        cbFilterKategori = new JComboBox<>(new String[]{"Semua", "Kuliah", "Project", "Lab", "Pribadi"});
        btnFilter        = new JButton("Filter");
        btnSelesai       = new JButton("Tandai Selesai");
        btnHapus         = new JButton("Hapus Task");
        jScrollPane1     = new JScrollPane();
        tableTasks       = new JTable();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Todo List");
        getContentPane().setBackground(BG_WHITE);

        btnTambah.addActionListener(this::btnTambahActionPerformed);
        btnSelesai.addActionListener(this::btnSelesaiActionPerformed);
        btnHapus.addActionListener(this::btnHapusActionPerformed);
        btnFilter.addActionListener(e -> loadData());
        tableTasks.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableTasksMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(tableTasks);

        // Panel input layout
        panelInput.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 4, 3, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: TASK label, KATEGORI label, (tambah)
        gc.gridy=0; gc.gridx=0; gc.weightx=0; panelInput.add(lblTask, gc);
        gc.gridx=1; gc.weightx=1; panelInput.add(txtTask, gc);
        gc.gridx=2; gc.weightx=0; panelInput.add(lblKategori, gc);
        gc.gridx=3; gc.weightx=0; panelInput.add(cbKategori, gc);
        gc.gridx=4; gc.weightx=0; panelInput.add(btnTambah, gc);

        // Row 1: DEADLINE
        gc.gridy=1; gc.gridx=0; gc.weightx=0; panelInput.add(lblDeadline, gc);
        gc.gridx=1; gc.weightx=0;
        txtDeadline.setPreferredSize(new Dimension(120, 28));
        panelInput.add(txtDeadline, gc);
        gc.gridx=2; gc.gridwidth=3; panelInput.add(lblDeadlineHint, gc);
        gc.gridwidth=1;

        // Main layout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(lblJudul)
            .addComponent(panelInput)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblFilter)
                .addComponent(cbFilterKategori, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnFilter))
            .addGroup(layout.createSequentialGroup()
                .addComponent(btnSelesai)
                .addComponent(btnHapus))
            .addComponent(jScrollPane1, 0, 560, Short.MAX_VALUE)
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
            .addComponent(lblJudul)
            .addGap(8)
            .addComponent(panelInput, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(lblFilter)
                .addComponent(cbFilterKategori, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(btnFilter))
            .addGap(8)
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(btnSelesai)
                .addComponent(btnHapus))
            .addGap(8)
            .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }

    // =========================================================
    // EVENT HANDLERS
    // =========================================================
    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {
        String task     = txtTask.getText().trim();
        String kategori = (String) cbKategori.getSelectedItem();
        String deadline = txtDeadline.getText().trim();

        if (task.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Task tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        java.sql.Date sqlDeadline = null;
        if (!deadline.isEmpty()) {
            try {
                sqlDeadline = java.sql.Date.valueOf(deadline);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,
                        "Format deadline salah!\nGunakan format YYYY-MM-DD\nContoh: 2026-06-30",
                        "Format Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            String sql = "INSERT INTO tasks(title, kategori, status, deadline, created_at) VALUES (?,?,'pending',?,CURDATE())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, task);
            ps.setString(2, kategori);
            if (sqlDeadline != null) ps.setDate(3, sqlDeadline);
            else ps.setNull(3, java.sql.Types.DATE);

            if (ps.executeUpdate() > 0) {
                txtTask.setText("");
                txtDeadline.setText("");
                loadData();
                JOptionPane.showMessageDialog(this, "Task berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btnSelesaiActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task dulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET status='done' WHERE id=?");
            ps.setInt(1, selectedTaskId);
            ps.executeUpdate();
            selectedTaskId = -1;
            loadData();
            JOptionPane.showMessageDialog(this, "Task selesai!", "Selesai", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {
        if (selectedTaskId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih task dulu!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin hapus task ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id=?");
            ps.setInt(1, selectedTaskId);
            ps.executeUpdate();
            selectedTaskId = -1;
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tableTasksMouseClicked(java.awt.event.MouseEvent evt) {
        int row = tableTasks.getSelectedRow();
        if (row != -1) {
            selectedTaskId = Integer.parseInt(tableTasks.getValueAt(row, 0).toString());
        }
    }

    // =========================================================
    // MAIN
    // =========================================================
    public static void main(String args[]) {
        try {
            // Pakai Look and Feel bawaan sistem biar lebih native & rapi
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> new DataTasks().setVisible(true));
    }

    // =========================================================
    // VARIABLE DECLARATION
    // =========================================================
    private JButton btnTambah, btnSelesai, btnHapus, btnFilter;
    private JScrollPane jScrollPane1;
    private JLabel lblJudul, lblTask, lblKategori, lblDeadline, lblDeadlineHint, lblFilter;
    private JTable tableTasks;
    private JTextField txtTask, txtDeadline;
    private JComboBox<String> cbKategori, cbFilterKategori;
    private JPanel panelInput;
}
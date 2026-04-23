import javax.swing.*;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

abstract class Reservation {
    String customerName;
    int peopleCount;
    LocalDate date;
    LocalTime time;

    static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    Reservation(String customerName, int peopleCount, LocalDate date, LocalTime time) {
        this.customerName = customerName;
        this.peopleCount = peopleCount;
        this.date = date;
        this.time = time;
    }

    abstract String getType();
    abstract String getExtra();
    abstract String toFileString();
}

class IndoorReservation extends Reservation {
    int tableNumber;
    String seatingType;

    IndoorReservation(String name, int people, LocalDate date, LocalTime time, int tableNumber, String seatingType) {
        super(name, people, date, time);
        this.tableNumber = tableNumber;
        this.seatingType = seatingType;
    }

    String getType() {
        return "Вътрешна";
    }

    String getExtra() {
        return "Маса: " + tableNumber + ", Настаняване: " + seatingType;
    }

    String toFileString() {
        return "INDOOR|" + customerName + "|" + peopleCount + "|"
                + date.format(Reservation.DF) + "|" + time.format(Reservation.TF) + "|"
                + tableNumber + "|" + seatingType;
    }
}

class OutdoorReservation extends Reservation {
    String outdoorArea;
    String smokingType;

    OutdoorReservation(String name, int people, LocalDate date, LocalTime time, String outdoorArea, String smokingType) {
        super(name, people, date, time);
        this.outdoorArea = outdoorArea;
        this.smokingType = smokingType;
    }

    String getType() {
        return "Външна";
    }

    String getExtra() {
        return "Зона: " + outdoorArea + ", Секция: " + smokingType;
    }

    String toFileString() {
        return "OUTDOOR|" + customerName + "|" + peopleCount + "|"
                + date.format(Reservation.DF) + "|" + time.format(Reservation.TF) + "|"
                + outdoorArea + "|" + smokingType;
    }
}

public class ReservationDeskGUI extends JFrame {

    static final ArrayList<Reservation> list = new ArrayList<>();
    static final int MAX_CAPACITY = 200;
    static final int INDOOR_CAPACITY = 120;
    static final int OUTDOOR_CAPACITY = 80;
    static final String FILE_NAME = "reservations.txt";

    JTextField nameField = new JTextField();
    JTextField peopleField = new JTextField();
    JTextField tableField = new JTextField();
    JTextField searchField = new JTextField();

    SpinnerDateModel dateModel = new SpinnerDateModel();
    SpinnerDateModel timeModel = new SpinnerDateModel();
    JSpinner dateSpinner = new JSpinner(dateModel);
    JSpinner timeSpinner = new JSpinner(timeModel);

    JComboBox<String> seatingBox = new JComboBox<>(new String[]{"Маса", "Сепаре", "Дълга маса"});
    JComboBox<String> outdoorAreaBox = new JComboBox<>(new String[]{"Градина", "Тераса"});
    JComboBox<String> smokingBox = new JComboBox<>(new String[]{"Пушачи", "Непушачи"});
    JComboBox<String> filterTypeBox = new JComboBox<>(new String[]{"Всички", "Вътрешна", "Външна"});

    JRadioButton indoorBtn = new JRadioButton("Вътрешна", true);
    JRadioButton outdoorBtn = new JRadioButton("Външна");

    JLabel modeLabel = new JLabel("Режим: Добавяне");
    JLabel seatingNoteLabel = new JLabel("За до 5 човека настаняването е автоматично: Маса");
    JLabel summaryLabel = new JLabel();
    JLabel occupancyLabel = new JLabel();

    int editIndex = -1;

    DefaultTableModel model = new DefaultTableModel(
            new String[]{"№", "Клиент", "Хора", "Дата", "Час", "Тип", "Детайли"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    JTable table = new JTable(model);
    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

    JSpinner filterDateSpinner = new JSpinner(new SpinnerDateModel());
    JCheckBox useDateFilter = new JCheckBox("Използвай дата");

    JButton addBtn = new JButton("Добави");
    JButton editBtn = new JButton("Зареди за редакция");
    JButton saveEditBtn = new JButton("Запази промените");
    JButton deleteBtn = new JButton("Изтрий избраната");
    JButton cancelEditBtn = new JButton("Откажи редакция");
    JButton clearFilterBtn = new JButton("Изчисти филтъра");
    JButton saveBtn = new JButton("Запази във файл");

    public ReservationDeskGUI() {
        setTitle("Reservation Desk App");
        setSize(1150, 740);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd.MM.yyyy"));
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));
        filterDateSpinner.setEditor(new JSpinner.DateEditor(filterDateSpinner, "dd.MM.yyyy"));

        JPanel mainTop = new JPanel(new BorderLayout(10, 10));
        JPanel formPanel = new JPanel(new GridLayout(11, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Добавяне / редакция на резервация"));

        ButtonGroup group = new ButtonGroup();
        group.add(indoorBtn);
        group.add(outdoorBtn);

        formPanel.add(new JLabel("Име:"));
        formPanel.add(nameField);

        formPanel.add(new JLabel("Брой хора:"));
        formPanel.add(peopleField);

        formPanel.add(new JLabel("Дата:"));
        formPanel.add(dateSpinner);

        formPanel.add(new JLabel("Час:"));
        formPanel.add(timeSpinner);

        formPanel.add(new JLabel("Тип резервация:"));
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(indoorBtn);
        typePanel.add(outdoorBtn);
        formPanel.add(typePanel);

        formPanel.add(new JLabel("Номер на маса (вътре):"));
        formPanel.add(tableField);

        formPanel.add(new JLabel("Настаняване (само над 5 човека):"));
        formPanel.add(seatingBox);

        formPanel.add(new JLabel("Външна зона:"));
        formPanel.add(outdoorAreaBox);

        formPanel.add(new JLabel("Секция:"));
        formPanel.add(smokingBox);

        formPanel.add(new JLabel("Бележка:"));
        formPanel.add(seatingNoteLabel);

        JPanel topInfoPanel = new JPanel(new BorderLayout());
        topInfoPanel.add(modeLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new GridLayout(1, 5, 8, 8));
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(saveEditBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(cancelEditBtn);

        mainTop.add(topInfoPanel, BorderLayout.NORTH);
        mainTop.add(formPanel, BorderLayout.CENTER);
        mainTop.add(actionPanel, BorderLayout.SOUTH);
        add(mainTop, BorderLayout.NORTH);

        table.setRowSorter(sorter);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomWrap = new JPanel(new BorderLayout(10, 10));

        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 8, 8));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Търсене и филтър"));
        filterPanel.add(new JLabel("Търси по име:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Тип:"));
        filterPanel.add(filterTypeBox);
        filterPanel.add(new JLabel("Дата:"));
        filterPanel.add(filterDateSpinner);
        filterPanel.add(useDateFilter);
        filterPanel.add(clearFilterBtn);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 4, 4));
        JPanel row1 = new JPanel(new BorderLayout());
        JPanel row2 = new JPanel(new BorderLayout());
        JPanel row3 = new JPanel(new BorderLayout());
        row1.add(summaryLabel, BorderLayout.WEST);
        row2.add(occupancyLabel, BorderLayout.WEST);
        row3.add(saveBtn, BorderLayout.EAST);
        infoPanel.add(row1);
        infoPanel.add(row2);
        infoPanel.add(row3);

        bottomWrap.add(filterPanel, BorderLayout.NORTH);
        bottomWrap.add(infoPanel, BorderLayout.SOUTH);
        add(bottomWrap, BorderLayout.SOUTH);

        indoorBtn.addActionListener(e -> refreshFormState());
        outdoorBtn.addActionListener(e -> refreshFormState());

        peopleField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshFormState(); }
            public void removeUpdate(DocumentEvent e) { refreshFormState(); }
            public void changedUpdate(DocumentEvent e) { refreshFormState(); }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        filterTypeBox.addActionListener(e -> applyFilter());
        useDateFilter.addActionListener(e -> applyFilter());
        filterDateSpinner.addChangeListener(e -> applyFilter());
        dateSpinner.addChangeListener(e -> updateTable());
        timeSpinner.addChangeListener(e -> updateTable());

        clearFilterBtn.addActionListener(e -> clearFilter());
        addBtn.addActionListener(e -> addReservation());
        editBtn.addActionListener(e -> loadSelectedForEdit());
        saveEditBtn.addActionListener(e -> saveEditedReservation());
        deleteBtn.addActionListener(e -> deleteReservation());
        cancelEditBtn.addActionListener(e -> clearEditMode());
        saveBtn.addActionListener(e -> saveWithMessage());

        loadFromFile();
        setDefaultValues();
        clearEditMode();
        updateTable();
    }

    void refreshFormState() {
        boolean indoor = indoorBtn.isSelected();
        boolean bigGroup = getPeopleInput() > 5;

        tableField.setEnabled(indoor);
        seatingBox.setEnabled(indoor && bigGroup);
        outdoorAreaBox.setEnabled(!indoor);
        smokingBox.setEnabled(!indoor);

        if (bigGroup) {
            seatingNoteLabel.setText("Избери: Маса, Сепаре или Дълга маса");
        } else {
            seatingNoteLabel.setText("За до 5 човека настаняването е автоматично: Маса");
        }
    }

    void setDefaultValues() {
        Date now = new Date();
        dateSpinner.setValue(now);
        timeSpinner.setValue(now);
    }

    int getPeopleInput() {
        try {
            return Integer.parseInt(peopleField.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    LocalDate getSelectedDate() {
        Date d = (Date) dateSpinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    LocalTime getSelectedTime() {
        Date d = (Date) timeSpinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    LocalDate getFilterDate() {
        Date d = (Date) filterDateSpinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    void applyFilter() {
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String name = entry.getStringValue(1).toLowerCase();
                String date = entry.getStringValue(3);
                String type = entry.getStringValue(5);

                String q = searchField.getText().trim().toLowerCase();
                String selectedType = (String) filterTypeBox.getSelectedItem();

                boolean okName = q.isEmpty() || name.contains(q);
                boolean okType = selectedType.equals("Всички") || type.equals(selectedType);
                boolean okDate = true;

                if (useDateFilter.isSelected()) {
                    okDate = date.equals(getFilterDate().format(Reservation.DF));
                }

                return okName && okType && okDate;
            }
        });
    }

    void clearFilter() {
        searchField.setText("");
        filterTypeBox.setSelectedIndex(0);
        useDateFilter.setSelected(false);
        sorter.setRowFilter(null);
    }

    Reservation buildReservationFromForm(boolean editing) {
        String name = nameField.getText().trim();
        String peopleText = peopleField.getText().trim();

        if (name.isEmpty() || peopleText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Попълни име и брой хора.");
            return null;
        }

        int people;
        try {
            people = Integer.parseInt(peopleText);
            if (people <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Броят хора трябва да е положително число.");
            return null;
        }

        LocalDate date = getSelectedDate();
        LocalTime time = getSelectedTime();

        if (date.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Не може да се прави резервация за минала дата.");
            return null;
        }

        int totalAtSlot = getTotalPeopleForDateTime(date, time);
        int indoorAtSlot = getIndoorPeopleForDateTime(date, time);
        int outdoorAtSlot = getOutdoorPeopleForDateTime(date, time);

        if (editing && editIndex >= 0) {
            Reservation old = list.get(editIndex);
            if (old.date.equals(date) && old.time.equals(time)) {
                totalAtSlot -= old.peopleCount;
                if (old instanceof IndoorReservation) {
                    indoorAtSlot -= old.peopleCount;
                } else {
                    outdoorAtSlot -= old.peopleCount;
                }
            }
        }

        if (totalAtSlot + people > MAX_CAPACITY) {
            JOptionPane.showMessageDialog(this, "Няма достатъчно места за този ден и час. Общ капацитет: 200.");
            return null;
        }

        if (indoorBtn.isSelected()) {
            if (indoorAtSlot + people > INDOOR_CAPACITY) {
                JOptionPane.showMessageDialog(this, "Няма достатъчно места във вътрешния салон за този ден и час. Капацитет: 120.");
                return null;
            }

            String tableText = tableField.getText().trim();
            if (tableText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Въведи номер на маса.");
                return null;
            }

            int tableNumber;
            try {
                tableNumber = Integer.parseInt(tableText);
                if (tableNumber <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Номерът на масата трябва да е положително число.");
                return null;
            }

            if (isTableTaken(tableNumber, date, time, editing ? editIndex : -1)) {
                JOptionPane.showMessageDialog(this, "Масата е заета за този ден и час.");
                return null;
            }

            String seatingType = people > 5 ? (String) seatingBox.getSelectedItem() : "Маса";
            return new IndoorReservation(name, people, date, time, tableNumber, seatingType);
        }

        if (outdoorAtSlot + people > OUTDOOR_CAPACITY) {
            JOptionPane.showMessageDialog(this, "Няма достатъчно места навън за този ден и час. Капацитет: 80.");
            return null;
        }

        String outdoorArea = (String) outdoorAreaBox.getSelectedItem();
        String smokingType = (String) smokingBox.getSelectedItem();
        return new OutdoorReservation(name, people, date, time, outdoorArea, smokingType);
    }

    void addReservation() {
        Reservation r = buildReservationFromForm(false);
        if (r == null) {
            return;
        }

        list.add(r);
        updateTable();
        clearFieldsOnly();
        saveToFile();
        JOptionPane.showMessageDialog(this, "Резервацията е добавена успешно.");
    }

    void loadSelectedForEdit() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Избери резервация за редакция.");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        editIndex = row;
        Reservation r = list.get(row);

        nameField.setText(r.customerName);
        peopleField.setText(String.valueOf(r.peopleCount));

        Date dateValue = Date.from(r.date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date timeValue = Date.from(r.date.atTime(r.time).atZone(ZoneId.systemDefault()).toInstant());
        dateSpinner.setValue(dateValue);
        timeSpinner.setValue(timeValue);

        if (r instanceof IndoorReservation) {
            IndoorReservation ir = (IndoorReservation) r;
            indoorBtn.setSelected(true);
            tableField.setText(String.valueOf(ir.tableNumber));
            seatingBox.setSelectedItem(ir.seatingType);
        } else {
            OutdoorReservation or = (OutdoorReservation) r;
            outdoorBtn.setSelected(true);
            outdoorAreaBox.setSelectedItem(or.outdoorArea);
            smokingBox.setSelectedItem(or.smokingType);
            tableField.setText("");
        }

        modeLabel.setText("Режим: Редакция");
        refreshFormState();
    }

    void saveEditedReservation() {
        if (editIndex < 0) {
            JOptionPane.showMessageDialog(this, "Първо зареди резервация за редакция.");
            return;
        }

        Reservation updated = buildReservationFromForm(true);
        if (updated == null) {
            return;
        }

        list.set(editIndex, updated);
        updateTable();
        saveToFile();
        JOptionPane.showMessageDialog(this, "Резервацията е редактирана успешно.");
        clearEditMode();
    }

    void clearEditMode() {
        editIndex = -1;
        modeLabel.setText("Режим: Добавяне");
        clearFieldsOnly();
        indoorBtn.setSelected(true);
        refreshFormState();
    }

    void clearFieldsOnly() {
        nameField.setText("");
        peopleField.setText("");
        tableField.setText("");
        seatingBox.setSelectedIndex(0);
        outdoorAreaBox.setSelectedIndex(0);
        smokingBox.setSelectedIndex(0);
        setDefaultValues();
        updateTable();
    }

    boolean isTableTaken(int tableNumber, LocalDate date, LocalTime time, int skipIndex) {
        for (int i = 0; i < list.size(); i++) {
            if (i == skipIndex) {
                continue;
            }

            Reservation r = list.get(i);
            if (r instanceof IndoorReservation) {
                IndoorReservation ir = (IndoorReservation) r;
                if (ir.tableNumber == tableNumber && ir.date.equals(date) && ir.time.equals(time)) {
                    return true;
                }
            }
        }
        return false;
    }

    int getTotalPeople() {
        int sum = 0;
        for (Reservation r : list) {
            sum += r.peopleCount;
        }
        return sum;
    }

    int getTotalPeopleForDateTime(LocalDate date, LocalTime time) {
        int sum = 0;
        for (Reservation r : list) {
            if (r.date.equals(date) && r.time.equals(time)) {
                sum += r.peopleCount;
            }
        }
        return sum;
    }

    int getIndoorPeopleForDateTime(LocalDate date, LocalTime time) {
        int sum = 0;
        for (Reservation r : list) {
            if (r instanceof IndoorReservation && r.date.equals(date) && r.time.equals(time)) {
                sum += r.peopleCount;
            }
        }
        return sum;
    }

    int getOutdoorPeopleForDateTime(LocalDate date, LocalTime time) {
        int sum = 0;
        for (Reservation r : list) {
            if (r instanceof OutdoorReservation && r.date.equals(date) && r.time.equals(time)) {
                sum += r.peopleCount;
            }
        }
        return sum;
    }

    void updateTable() {
        model.setRowCount(0);

        int i = 1;
        for (Reservation r : list) {
            model.addRow(new Object[]{
                    i,
                    r.customerName,
                    r.peopleCount,
                    r.date.format(Reservation.DF),
                    r.time.format(Reservation.TF),
                    r.getType(),
                    r.getExtra()
            });
            i++;
        }

        LocalDate d = getSelectedDate();
        LocalTime t = getSelectedTime();
        int totalAtSlot = getTotalPeopleForDateTime(d, t);
        int indoorAtSlot = getIndoorPeopleForDateTime(d, t);
        int outdoorAtSlot = getOutdoorPeopleForDateTime(d, t);

        summaryLabel.setText("Общо резервации: " + list.size() + " | Общо хора: " + getTotalPeople());
        occupancyLabel.setText(
                "Заетост за " + d.format(Reservation.DF) + " " + t.format(Reservation.TF)
                        + " | Общо: " + totalAtSlot + "/" + MAX_CAPACITY + " (свободни: " + (MAX_CAPACITY - totalAtSlot) + ")"
                        + " | Вътре: " + indoorAtSlot + "/" + INDOOR_CAPACITY + " (свободни: " + (INDOOR_CAPACITY - indoorAtSlot) + ")"
                        + " | Навън: " + outdoorAtSlot + "/" + OUTDOOR_CAPACITY + " (свободни: " + (OUTDOOR_CAPACITY - outdoorAtSlot) + ")"
        );
    }

    void deleteReservation() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Избери резервация за изтриване.");
            return;
        }

        int row = table.convertRowIndexToModel(viewRow);
        list.remove(row);
        updateTable();
        saveToFile();
        JOptionPane.showMessageDialog(this, "Резервацията е изтрита.");
        clearEditMode();
    }

    void saveWithMessage() {
        saveToFile();
        JOptionPane.showMessageDialog(this, "Данните са записани в " + FILE_NAME);
    }

    void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Reservation r : list) {
                writer.println(r.toFileString());
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Грешка при запис във файл.");
        }
    }

    void loadFromFile() {
        list.clear();
        File f = new File(FILE_NAME);
        if (!f.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length < 7) {
                    continue;
                }

                String type = p[0];
                String name = p[1];
                int people = Integer.parseInt(p[2]);
                LocalDate date = LocalDate.parse(p[3], Reservation.DF);
                LocalTime time = LocalTime.parse(p[4], Reservation.TF);

                if (type.equals("INDOOR")) {
                    int tableNumber = Integer.parseInt(p[5]);
                    String seatingType = p[6];
                    list.add(new IndoorReservation(name, people, date, time, tableNumber, seatingType));
                } else if (type.equals("OUTDOOR")) {
                    String outdoorArea = p[5];
                    String smokingType = p[6];
                    list.add(new OutdoorReservation(name, people, date, time, outdoorArea, smokingType));
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Проблем при зареждане на файла.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationDeskGUI().setVisible(true));
    }
}

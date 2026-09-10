package app.view;

import app.lib.ConvertType;
import app.lib.ShowAppMsg;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Контролер діалогу очищення бази даних.
 * Перевіряє права доступу через kbase.access_user (тільки Postgres).
 * Для SQLite перевірка не потрібна.
 *
 * @author Igor Makarevich
 */
public class DBClear_Controller {
    /** ID типу доступу для очищення БД (відповідає kbase.access_type.id = 1) */
    private static final int ACCESS_TYPE_CLEAR_DB = 1;

    // ======== Controls ========
    @FXML
    private ImageView imageView_Icon;

    @FXML
    private Label label_DBName;

    /** Документи (public.documents) */
    @FXML
    private CheckBox checkBox_Documents;

    /** Інфоблоки (kbase.info, info_text, info_image, info_file, dict) */
    @FXML
    private CheckBox checkBox_Info;
    /**
     * Розділи (kbase.sections).
     * При виборі — автоматично вмикає Documents та Info (FK-залежність).
     */
    @FXML
    private CheckBox checkBox_Sections;
    /**
     * Шаблони (template, template_style, template_files, template_themes тощо).
     * Незалежний від розділів (зв'язок тільки varchar-тегом).
     * При виборі — попереджає, якщо шаблони використовуються в стилях.
     */
    @FXML
    private CheckBox checkBox_Templates;

    /** Іконки (public.icons). */
    @FXML
    private CheckBox checkBox_Icons;

    @FXML
    private Label label_Warning;

    @FXML
    private Button button_Cancel;

    @FXML
    private Button button_Clear;

    // ======== State ========
    private Params params;
    private DBConCur_Parameters conCur;

    /**
     * Конструктор (викликається до initialize()).
     */
    public DBClear_Controller() {
    }

    /**
     * Автоматично викликається після завантаження FXML.
     * Встановлює іконки кнопок та логіку чекбоксів.
     */
    @FXML
    private void initialize() {
        button_Cancel.setGraphic(new ImageView(new Image("file:resources/images/icon_cancel_16.png")));
        button_Clear.setGraphic(new ImageView(new Image("file:resources/images/icon_DBClear_16.png")));

        // Логіка залежності чекбоксів:
        // Вибір "Розділи" → автоматично вмикає "Документи" та "Інфоблоки" (FK)
        checkBox_Sections.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                checkBox_Documents.setSelected(true);
                checkBox_Info.setSelected(true);
            }
        });
        // Якщо знімаємо "Документи" або "Інфоблоки" — знімаємо і "Розділи" (бо їх не можна видалити без них)
        checkBox_Documents.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                checkBox_Sections.setSelected(false);
            }
        });
        checkBox_Info.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                checkBox_Sections.setSelected(false);
            }
        });

        // Іконки: якщо знімаємо — попередження (розділи посилаються на іконки, але FK не жорсткий)
        checkBox_Icons.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && checkBox_Sections.isSelected()) {
                label_Warning.setText(
                    "⚠ Увага! Розділи посилаються на іконки — після видалення іконок " +
                    "деякі посилання можуть стати недійсними.");
            } else {
                label_Warning.setText("⚠ Увага! Видалення є незворотньою операцією.");
            }
        });
    }

    /**
     * Ініціалізація з параметрами — викликається з Root_Controller.
     * Перевіряє права доступу; якщо немає — закриває діалог з повідомленням.
     *
     * @param params параметри програми (params.getConCur() — активне з'єднання)
     */
    public void setParams(Params params) {
        this.params  = params;
        this.conCur  = params.getConCur();

        initDBNameLabel();
        checkAccessAndInit();
    }

    /**
     * Підфарбовує label з назвою БД кольором з'єднання (з налаштувань).
     */
    private void initDBNameLabel() {
        if (conCur == null) return;

        DBConn_Parameters p = conCur.param;

        // Іконка БД
        imageView_Icon.setImage(new Image("file:resources/images/icon_DBClear_16.png"));

        // Назва БД
        String dbName = (p.getName() != null && !p.getName().isEmpty())
                ? p.getName()
                : p.getConnName();
        label_DBName.setText(dbName);

        // Колір тексту та фону з налаштувань з'єднання (активний стан)
        if (p.getColorEnable()) {
            String textColor = ConvertType.colorToHex(
                new Color(p.getColorTRed_A(), p.getColorTGreen_A(),
                          p.getColorTBlue_A(), p.getColorTOpacity_A()));
            String bgColor = ConvertType.colorToHex(
                new Color(p.getColorBRed_A(), p.getColorBGreen_A(),
                          p.getColorBBlue_A(), p.getColorBOpacity_A()));
            label_DBName.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-text-fill: #" + textColor + "; " +
                "-fx-background-color: #" + bgColor + "; " +
                "-fx-padding: 4px 8px; -fx-background-radius: 4px;");
        }
    }

    /**
     * Перевіряє права доступу через kbase.access_user.
     * Для SQLite — права не перевіряються (accessGet завжди повертає true).
     * Якщо прав немає — показує повідомлення і закриває вікно.
     */
    private void checkAccessAndInit() {
        if (conCur == null) {
            closeStage();
            return;
        }

        boolean hasAccess = conCur.db.accessGet(ACCESS_TYPE_CLEAR_DB);

        if (!hasAccess) {
            ShowAppMsg.showAlert("WARNING", "Доступ заборонено",
                "У вас немає прав для очищення бази даних",
                "Зверніться до адміністратора.\n" +
                "Поточний користувач: " + conCur.db.getCurrentUser());
            closeStage();
        }
    }

    /**
     * Обробник кнопки "Відміна" — просто закриває вікно.
     */
    @FXML
    private void handleCancel() {
        closeStage();
    }

    /**
     * Обробник кнопки "Очистити":
     * 1. Перевіряє, чи вибрано хоча б один тип.
     * 2. Попереджає про видалення шаблонів, якщо вони використовуються в інфоблоках.
     * 3. Запитує підтвердження.
     * 4. Виконує очищення через db.dbClear().
     * 5. Показує повідомлення про успіх.
     */
    @FXML
    private void handleClear() {
        boolean doDocs      = checkBox_Documents.isSelected();
        boolean doInfo      = checkBox_Info.isSelected();
        boolean doSections  = checkBox_Sections.isSelected();
        boolean doTemplates = checkBox_Templates.isSelected();
        boolean doIcons     = checkBox_Icons.isSelected();

        // Перевірка: хоча б один тип обраний
        if (!doDocs && !doInfo && !doSections && !doTemplates && !doIcons) {
            ShowAppMsg.showAlert("INFORMATION", "Очищення БД",
                "Нічого не вибрано",
                "Виберіть хоча б один тип даних для видалення.");
            return;
        }

        // Перевірка використання шаблонів в інфоблоках (якщо шаблони видаляємо, але інфо — ні)
        if (doTemplates && !doInfo) {
            boolean proceed = ShowAppMsg.showQuestion("CONFIRMATION",
                "Перевірка залежностей",
                "Шаблони використовуються в інфоблоках",
                "Деякі інфоблоки можуть посилатись на стилі шаблонів, " +
                "які будуть видалені.\nПродовжити?");
            if (!proceed) return;
        }

        // Формуємо опис операції
        StringBuilder sb = new StringBuilder("Буде видалено:\n");
        if (doDocs)      sb.append("  • Документи\n");
        if (doInfo)      sb.append("  • Інфоблоки\n");
        if (doSections)  sb.append("  • Розділи\n");
        if (doTemplates) sb.append("  • Шаблони\n");
        if (doIcons)     sb.append("  • Іконки\n");
        sb.append("\nОперація незворотня!");

        // Підтвердження
        boolean confirmed = ShowAppMsg.showQuestion("CONFIRMATION",
            "Підтвердження очищення БД",
            "Ви впевнені?",
            sb.toString());
        if (!confirmed) return;

        // Виконуємо очищення
        conCur.db.dbClear(doDocs, doInfo, doSections, doTemplates, doIcons);

        // Повідомлення про завершення
        ShowAppMsg.showAlert("INFORMATION", "Очищення БД",
            "Очищення завершено успішно",
            "Дані видалено з бази даних: " + conCur.param.getName());

        closeStage();
    }

    /**
     * Закриває поточне вікно діалогу.
     */
    private void closeStage() {
        Stage stage = (Stage) button_Cancel.getScene().getWindow();
        stage.close();
    }
}

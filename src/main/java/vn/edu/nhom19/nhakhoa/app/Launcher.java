package vn.edu.nhom19.nhakhoa.app;

import javafx.application.Application;

/** Điểm vào kỹ thuật; không chứa nghiệp vụ phòng khám. */
public final class Launcher {
    private Launcher() { }

    public static void main(String[] args) {
        Application.launch(DentaFlowApp.class, args);
    }
}

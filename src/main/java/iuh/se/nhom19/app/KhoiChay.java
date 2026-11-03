package iuh.se.nhom19.app;

import javafx.application.Application;

/** Điểm vào kỹ thuật; không chứa nghiệp vụ phòng khám. */
public final class KhoiChay {
    private KhoiChay() { }

    public static void main(String[] thamSo) {
        Application.launch(UngDungDentaFlow.class, thamSo);
    }
}

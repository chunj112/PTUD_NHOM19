package vn.edu.nhom19.nhakhoa.view.common;

/** Vòng đời một trang trong AppShell; dùng để chặn rời trang khi còn dữ liệu chưa lưu. */
public class ScreenController {
    protected final ControllerContext context;
    public ViewAccess view;
    protected ScreenController(ViewAccess view, ControllerContext context) { this.view=view;this.context=context; }
    public void refresh() { }
    public boolean canLeave() { return true; }
    public void dispose() { }
}

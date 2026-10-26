package vn.edu.nhom19.nhakhoa;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class ProjectStructureTest {
    @Test
    void starterResourcesExist() {
        assertNotNull(getClass().getResource("/fxml/common/app-shell.fxml"));
        assertNotNull(getClass().getResource("/css/theme.css"));
        assertNotNull(getClass().getResource("/images/dentaflow-logo.png"));
    }
}

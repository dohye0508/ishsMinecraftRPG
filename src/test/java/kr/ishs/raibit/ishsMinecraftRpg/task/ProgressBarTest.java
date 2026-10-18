package kr.ishs.raibit.ishsMinecraftRpg.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgressBarTest {

    @Test
    void fillsProportionally() {
        assertEquals("##....", ProgressBar.render(0.33, 6, "", "#", "", "."));
        assertEquals("#####.", ProgressBar.render(0.84, 6, "", "#", "", "."));
    }

    @Test
    void clampsAndHandlesBadValues() {
        assertEquals("......", ProgressBar.render(-1, 6, "", "#", "", "."));
        assertEquals("######", ProgressBar.render(5, 6, "", "#", "", "."));
        assertEquals("......", ProgressBar.render(Double.NaN, 6, "", "#", "", "."));
        assertEquals("", ProgressBar.render(0.5, 0, "", "#", "", "."));
    }

    @Test
    void putsColorCodesBeforeEachPart() {
        assertEquals("A##B..", ProgressBar.render(0.5, 4, "A", "#", "B", "."));
    }
}

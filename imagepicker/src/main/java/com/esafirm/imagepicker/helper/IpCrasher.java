package com.esafirm.imagepicker.helper;

public class IpCrasher {
    public static void openIssue() {
        throw new IllegalStateException("This should not happen. Please open an issue!");
    }
}

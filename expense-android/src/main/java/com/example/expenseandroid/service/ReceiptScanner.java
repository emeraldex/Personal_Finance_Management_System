package com.example.expenseandroid.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ReceiptScanner {
    private final Tesseract tesseract;

    public ReceiptScanner() {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath("tessdata"); // Path to Tesseract data files
    }

    public String scanReceipt(File receiptImage) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(receiptImage);
        return tesseract.doOCR(image);
    }
}

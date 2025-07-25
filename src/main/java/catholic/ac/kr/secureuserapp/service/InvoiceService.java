package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.Sex;
import catholic.ac.kr.secureuserapp.model.entity.Order;
import catholic.ac.kr.secureuserapp.model.entity.OrderItem;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Service
public class InvoiceService {

    public byte[] generateInvoicePdf(Order order) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); //lưu dữ liệu PDF tạm thời trong bộ nhớ.

        Document document = new Document(); //Tạo mới file PDF
        PdfWriter.getInstance(document, outputStream);

        document.open();
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        document.add(new Paragraph("INVOICE - BOOKSTORE", boldFont));
        document.add(new Paragraph("Num: " + order.getId()));
        document.add(new Paragraph("Order date :" + order.getCreatedAt()));
        //kiểm tra giới tính khách hàng
        Sex sex = order.getUser().getSex();
        if (sex == Sex.MALE) {
            document.add(new Paragraph("Customer: Mr. " + order.getUser().getUsername()));
        } else if (sex == Sex.FEMALE) {
            document.add(new Paragraph("Customer: Mrs." + order.getUser().getUsername()));
        } else
            document.add(new Paragraph("Customer: " + order.getUser().getUsername()));
        document.add(new Paragraph("Discount as Coupon:" + order.getTotalDiscount() + "won"));
        document.add(new Paragraph(" "));

        PdfPTable table = getPdfPTable(order);

        document.add(table);
        document.add(new Paragraph("Total: " + order.getTotalPrice() + "won"));

        document.close();

        return outputStream.toByteArray();
    }

    private static PdfPTable getPdfPTable(Order order) {
        PdfPTable table = new PdfPTable(5); //so cột
        table.addCell("Name");
        table.addCell("Quantity");
        table.addCell("Price");
        table.addCell("Sale Price");
        table.addCell("Sale Price(Price) x Quantity");


        for (OrderItem item : order.getOrderItems()) {
            table.addCell(item.getBook().getTitle());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(item.getBook().getPrice().toString());
            table.addCell(item.getBook().getSalePrice() != null
                    ? item.getBook().getSalePrice().toString()
                    : "-");
            table.addCell((item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).toString());
        }
        return table;
    }
}

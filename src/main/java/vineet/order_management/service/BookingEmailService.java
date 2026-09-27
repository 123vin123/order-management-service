package vineet.order_management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import vineet.order_management.model.Booking;

@Service
public class BookingEmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final boolean enabled;
    private final String fromAddress;

    public BookingEmailService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.from:no-reply@vineet-eservice.local}") String fromAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.enabled = enabled;
        this.fromAddress = fromAddress;
    }

    public boolean sendConfirmation(Booking booking) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (!enabled || mailSender == null) {
            return false;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            String status = booking.getStatus();
            String title = "Booking " + status.charAt(0) + status.substring(1).toLowerCase();
            String subject = title + " #" + booking.getReference();
            String emoji = status.equals("CANCELLED") ? "❌" : (status.equals("DELIVERED") ? "📦" : (status.equals("SHIPPED") ? "🚚" : "🎉"));
            
            String statusBg = status.equals("CANCELLED") ? "#fecaca" : (status.equals("DELIVERED") ? "#c6f6d5" : (status.equals("SHIPPED") ? "#fde68a" : (status.equals("CONFIRMED") ? "#ddd6fe" : "#bfdbfe")));
            String statusColor = status.equals("CANCELLED") ? "#991b1b" : (status.equals("DELIVERED") ? "#22543d" : (status.equals("SHIPPED") ? "#92400e" : (status.equals("CONFIRMED") ? "#5b21b6" : "#1e40af")));
            
            String introText = switch (status) {
                case "CANCELLED" -> "Your booking has been cancelled.";
                case "CONFIRMED" -> "Your booking has been confirmed and is being processed.";
                case "SHIPPED" -> "Your booking has been shipped and is on its way.";
                case "DELIVERED" -> "Your booking has been successfully delivered.";
                default -> "Your booking has been successfully placed. Here are your order details:";
            };
            
            helper.setFrom(fromAddress, "Vineet E-Service");
            helper.setTo(booking.getAccount().getEmail());
            helper.setSubject(subject + " " + emoji);
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <body style="margin: 0; padding: 0; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f6;">
                    <div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">
                        <!-- Header -->
                        <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px; text-align: center;">
                            <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 700;">%s</h1>
                            <p style="color: #e2e8f0; margin-top: 10px; font-size: 16px;">Thank you for choosing Vineet E-Service</p>
                        </div>
                        
                        <!-- Content -->
                        <div style="padding: 40px 30px;">
                            <p style="font-size: 18px; color: #2d3748; margin-top: 0;">Hello <strong style="color: #4a5568;">%s</strong>,</p>
                            <p style="color: #718096; font-size: 15px; line-height: 1.6;">%s</p>
                            
                            <!-- Details Card -->
                            <div style="background-color: #f8fafc; border-left: 4px solid #667eea; padding: 20px; margin: 25px 0; border-radius: 4px;">
                                <table style="width: 100%%; border-collapse: collapse;">
                                    <tr>
                                        <td style="padding: 8px 0; color: #a0aec0; font-size: 14px; text-transform: uppercase; font-weight: 600;">Reference No.</td>
                                        <td style="padding: 8px 0; color: #2d3748; font-weight: 700; text-align: right;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #a0aec0; font-size: 14px; text-transform: uppercase; font-weight: 600;">Service</td>
                                        <td style="padding: 8px 0; color: #2d3748; font-weight: 600; text-align: right;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #a0aec0; font-size: 14px; text-transform: uppercase; font-weight: 600;">Quantity</td>
                                        <td style="padding: 8px 0; color: #2d3748; font-weight: 600; text-align: right;">%d</td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 8px 0; color: #a0aec0; font-size: 14px; text-transform: uppercase; font-weight: 600;">Status</td>
                                        <td style="padding: 8px 0; text-align: right;">
                                            <span style="background-color: %s; color: %s; padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 700;">%s</span>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            
                            <!-- Total Section -->
                            <div style="border-top: 2px dashed #e2e8f0; padding-top: 20px; margin-top: 20px; text-align: center;">
                                <span style="color: #718096; font-size: 15px;">Total Amount</span>
                                <div style="color: #764ba2; font-size: 32px; font-weight: 800; margin-top: 5px;">₹%.2f</div>
                            </div>
                        </div>
                        
                        <!-- Footer -->
                        <div style="background-color: #f8fafc; padding: 25px; text-align: center; border-top: 1px solid #e2e8f0;">
                            <p style="color: #a0aec0; font-size: 13px; margin: 0;">Need help? Reply to this email to contact our support team.</p>
                            <p style="color: #cbd5e0; font-size: 12px; margin-top: 10px;">&copy; 2026 Vineet E-Service. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, 
                title,
                booking.getAccount().getName(),
                introText,
                booking.getReference(),
                booking.getProduct().getName(),
                booking.getQuantity(),
                statusBg,
                statusColor,
                booking.getStatus().toString(),
                booking.getTotal());
                
            helper.setText(htmlContent, true); // true indicates HTML
            
            mailSender.send(message);
            return true;
        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException exception) {
            System.err.println("Failed to send HTML email: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }
}
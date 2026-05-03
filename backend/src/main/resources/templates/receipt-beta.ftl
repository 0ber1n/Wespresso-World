<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Wespresso World — Order Receipt [beta]</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 40px auto; color: #333; }
        h1   { color: #6b3a2a; }
        .receipt-box { border: 1px solid #ddd; padding: 20px; border-radius: 8px; }
        .label { font-weight: bold; }
        .message-box { background: #fff3cd; padding: 12px; border-radius: 4px; margin-top: 16px;
                       border-left: 4px solid #ffc107; }
        .items { width: 100%; border-collapse: collapse; margin: 16px 0; }
        .items td, .items th { border-bottom: 1px solid #eee; padding: 8px; text-align: left; }
        .vuln-note { font-size: 11px; color: #999; margin-top: 24px; }
    </style>
</head>
<body>
    <h1>☕ Wespresso World</h1>
    <h2>Order Receipt <small style="color:#cc0000;">[beta]</small></h2>

    <div class="receipt-box">

        <p><span class="label">Order ID:</span> ${order.id}</p>
        <p><span class="label">Customer:</span> ${order.customerName}</p>
        <p><span class="label">Shipping To:</span> ${order.shippingAddress}</p>

        <table class="items">
            <tr><th>Item</th><th>Qty</th><th>Price</th></tr>
            <#list order.items as item>
            <tr>
                <td>${item.itemName}</td>
                <td>${item.quantity}</td>
                <td>${item.price}</td>
            </tr>
            </#list>
        </table>

        <p><span class="label">Total:</span> $${order.totalPrice}</p>

        <#if order.orderNote??>
        <div class="message-box">
            <span class="label">Order Note:</span>
            <p>${order.orderNote}</p>
        </div>
        </#if>

        <p class="vuln-note">Receipt generator v0.2-beta — powered by dynamic templates</p>

    </div>
</body>
</html>

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Chào mừng bạn đến với Chú Bé Rồng Online - Đăng Ký Tài Khoản</title>
	<link rel="stylesheet" href="https://forum.ngocrongonline.com/app/view/css/StyleSheet.css" type="text/css" />
	<link rel="stylesheet" href="https://forum.ngocrongonline.com/app/view/css/template.css" type="text/css" />
	<script src="/view/static/js/disable_devtools.js"></script>
	<link rel="shortcut icon" href='https://forum.ngocrongonline.com/app/view/images/favicon.png' type="image/x-icon" />
	<script type="text/javascript">
		var _gaq = _gaq || [];
		_gaq.push(['_setAccount', 'UA-22738816-4']);
		_gaq.push(['_setDomainName', '.teamobi.com']);
		_gaq.push(['_trackPageview']);

		(function() {
			var ga = document.createElement('script');
			ga.type = 'text/javascript';
			ga.async = true;
			ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
			var s = document.getElementsByTagName('script')[0];
			s.parentNode.insertBefore(ga, s);
		})();
	</script>
	<link rel="stylesheet" href="https://forum.ngocrongonline.com/app/view/css/w3.css">
</head>
<style>
		.snowEffect {
			position: fixed;
			width: 100%;
			height: 100%;
			left: 0;
			top: 0;
			z-index: 99;
			overflow: hidden;
			pointer-events: none;
		}

		#snowcanvas {
			position: fixed;
			z-index: 0;
		}

        /* Thêm CSS cho thông báo */
        .message {
            margin-top: 10px;
            padding: 10px;
            border-radius: 5px;
            font-weight: bold;
        }
        .message.success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .message.error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
</style>
<body>
<div class="snowEffect">
	<canvas id="snowcanvas" height="100%" width="100%"></canvas>
</div>
<div class="body_body">
	<div class="left_top"></div>
	<div class="bg_top">
		<div class="right_top"></div>
	</div>
	<div class="body-content">
		<div class="a" align="center"><img src="/images/logo_sk_he.png" height="90"/></div>
		<div id="top">
			<div class="link-more">
				<div class="h" align="center">
					<div class="bg_tree"></div>
					<div class="bg_noel"></div>
					<div class="menu2" style="background: #561d00;">
						<table width="100%" border="0" cellspacing="4">
							<tr class="menu">
								<td><a href="http://ngocrongprivate.com">Trang Chủ</a></td>
								<td id="selected"><a href="https://ngocrongprivate.com/forum">Diễn Đàn</a></td>
							</tr>
						</table>
					</div>
					<div class="body" style="text-align:center">
						<div style="font-size:10px;">Đăng ký tài khoản Chú Bé Rồng Online mới.</div>
						<center>
							<form id="registerForm" method="POST" name="register">
                                <input type="hidden" name="action" value="register" />
                                <input type="hidden" name="keySig" value="a511129a7ce15460414e6fe318eebc2b" />
								<input type="hidden" name="nav" value="" readonly="readonly" />
								<table>
									<tr>
										<td colspan=2><label for="user">Tài Khoản:</label></td>
										<td colspan=2><input name="user" type="text" value="" required /></td>
									</tr>
									<tr>
										<td colspan=2><label for="pass">Mật khẩu:</label></td>
										<td colspan=2><input name="pass" type="password" value="" required /></td>
									</tr>
                                    <tr>
										<td colspan=2><label for="repass">Nhập lại:</label></td>
										<td colspan=2><input name="repass" type="password" value="" required /></td>
									</tr>
								</table>
								<table>
									<tr>
										<td>
											<input type="radio" name="server" value="1" required /> Server 1 sao
										</td>
									</tr>
								</table>
                                <div id="registerMessage" class="message" style="display:none;"></div>
								<button type="submit" class="w3-button w3-red" value="Đăng Ký" id="button1" name="submit">Đăng Ký</button><br />
								<div style="font-size:10px;">
									(Đã có tài khoản?)<br>
									<a href="login">Đăng Nhập</a>
								</div>
							</form><br>
						</center>
					</div>
				</div>
				<br>
			</div><br>
		</div>
	</div>
	<div class="left_b_bottom">
		<div class="right_b_bottom">
			<div class="footer">
				<div class="left_bottom"></div>
				<div class="right_bottom"></div>
			</div>
		</div>
	</div>
	<div class="copyright"><br><b>Bản quyền thuộc về Chú Bé Rồng Online - 2013</b></div>
</div>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js" type="text/javascript"></script>
<script type="text/javascript">
$(document).ready(function() {
    $('#registerForm').submit(function(e) {
        e.preventDefault(); // Ngăn chặn form submit theo cách thông thường

        var form = $(this);
        // Đảm bảo URL này khớp với tên file PHP của bạn
        // Nếu file PHP của bạn là "auth_process.php", hãy đổi thành 'auth_process.php'
        var url = 'register_process.php'; 

        $.ajax({
            type: "POST",
            url: url,
            data: form.serialize(), // Gửi tất cả dữ liệu từ form
            dataType: "json", // Mong đợi phản hồi JSON từ server
            success: function(response) {
                var messageDiv = $('#registerMessage');
                messageDiv.css('display', 'block'); // Hiển thị div thông báo
                messageDiv.removeClass('success error'); // Xóa các class cũ

                if (response.status === 'success') {
                    messageDiv.addClass('success');
                    messageDiv.text(response.message);
                    if (response.redirect) {
                        setTimeout(function() {
                            window.location.href = response.redirect; // Chuyển hướng sau 1.5 giây
                        }, 1500);
                    }
                } else {
                    messageDiv.addClass('error');
                    messageDiv.text(response.message);
                }
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("AJAX Error: ", textStatus, errorThrown, jqXHR.responseText);
                var messageDiv = $('#registerMessage');
                messageDiv.css('display', 'block');
                messageDiv.addClass('error');
                messageDiv.text('Đã xảy ra lỗi kết nối. Vui lòng thử lại sau. (Mã lỗi: ' + jqXHR.status + ')');
            }
        });
    });
});
</script>

<script src="https://ngocrongonline.com/view/static/js/ThreeCanvas.js" type="text/javascript"></script>
<script src="https://ngocrongonline.com/view/static/js/Snow3d.js" type="text/javascript"></script>
<script src="https://ngocrongonline.com/view/static/js/animation.js?v4" type="text/javascript"></script>
<script src="/cdn-cgi/scripts/7d0fa10a/cloudflare-static/rocket-loader.min.js" data-cf-settings="57d9dd0f2fd997a3f6dac1b9-|49" defer></script></body>
</html>
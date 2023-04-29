$(function(){
	$("#sendBtn").click(send_letter);
	$("#deleteMsg").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var toName = $("#recipient-name").val();
	var content =$("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			data = $.parseJSON(data);
			if (data.code === 0){
				$("#hintBody").text("发送成功!");
			}else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	)
}

function delete_msg() {
	// 删除数据
	var messageId = $("#messageId").val();
	$.post(
		CONTEXT_PATH + "/letter/delete",
		{"messageId":messageId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code === 0){
				$("#hintBody").text("消息删除成功!");
				$(this).parents(".media").remove();
			}else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	)
}
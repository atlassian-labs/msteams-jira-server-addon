function CopyToClipboard(element) {
    const temp = $("<input/>");
    $("body").append(temp);
    temp.val($(element).text()).select();
    document.execCommand("copy");
    temp.remove();
}
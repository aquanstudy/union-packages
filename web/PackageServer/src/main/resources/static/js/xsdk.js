/**
 * Created by Administrator on 2017/5/20.
 */

var _confirmCallback = null;

String.prototype.trim=function(){
    return this.replace(/(^\s*)|(\s*$)/g, "");
}
String.prototype.ltrim=function(){
    return this.replace(/(^\s*)/g,"");
}
String.prototype.rtrim=function(){
    return this.replace(/(\s*$)/g,"");
}

function isEmpty(str) {

    return str != null && str != "undefined" && str.trim().length == 0;
}

function alertShow(nodeID, str) {
    $(nodeID).html('<div id="_tipDivID" class="alert alert-block xsdk_alert"> <strong>提示：</strong><span id="_tipAtID">&nbsp;</span> </div>');
    $("#_tipAtID").html(str);
    $(nodeID).show();
}

function alertHide(nodeID) {
    $(nodeID).hide();
}

function showTips(str) {

    if($("#_tipTxt").length > 0){
        $("#_tipTxt").html(str);
    }else{
        $('body').append('<div class="tip-modal"> <div class="modal" id="_tipModal"> <div class="modal-dialog"> <div class="modal-content"> <div class="modal-header"><h4 class="modal-title">提 示</h4> </div> <div class="modal-body"> <p id="_tipTxt">'+str+'</p> </div> <div class="modal-footer"> <button type="button" class="btn btn-primary" data-dismiss="modal">确  定</button> </div> </div> </div> </div> </div>');
    }

    $("#_tipModal").modal('show');

}

function confirmOKCallback() {
    if(_confirmCallback != null){
        _confirmCallback();
        _confirmCallback = null;
    }
}

function confirmCancelCallback() {
    _confirmCallback = null;
}

function showConfirm(str, okCallback) {

    _confirmCallback = okCallback;

    if($("#_confirmTxt").length > 0){
        $("#_confirmTxt").html(str);
    }else{
        $('body').append('<div class="tip-modal"> <div class="modal" id="_contextModal"> <div class="modal-dialog"> <div class="modal-content"> <div class="modal-header"><h4 class="modal-title">操作确认</h4> </div> <div class="modal-body"> <p id="_confirmTxt">'+str+'</p> </div> <div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal" onclick="javascript:confirmCancelCallback();">取  消</button> <button type="button" class="btn btn-primary" data-dismiss="modal" onclick="javascript:confirmOKCallback();">确  定</button> </div> </div> </div> </div> </div>');
    }

    $("#_contextModal").modal('show');

}

function loadFormData(jsonStr){
    //var obj = eval("("+jsonStr+")");
    var obj = jsonStr;
    var key,value,tagName,type,arr;
    for(x in obj){
        key = x;
        value = obj[x];

        $("[name='"+key+"'],[name='"+key+"[]']").each(function(){
            tagName = $(this)[0].tagName;
            type = $(this).attr('type');
            if(tagName=='INPUT'){
                if(type=='radio'){
                    $(this).attr('checked',$(this).val()==value);
                }else if(type=='checkbox'){
                    arr = value.split(',');
                    for(var i =0;i<arr.length;i++){
                        if($(this).val()==arr[i]){
                            $(this).attr('checked',true);
                            break;
                        }
                    }
                }else{
                    $(this).val(value);
                }
            }else if(tagName=='SELECT' || tagName=='TEXTAREA'){
                $(this).val(value);
            }

        });
    }
}

function dateBefore() {
    var d = new Date();

    d = new Date(d.getFullYear(),d.getMonth(),d.getDate()-n);
}

//获取url后面的参数
function getParameter(key)
{

    var reg = new RegExp("(^|&)"+key+"=([^&]*)(&|$)");
    var result = window.location.search.substr(1).match(reg);
    return result?decodeURIComponent(result[2]):null;

    //var query = window.location.search;
    //
    //var iLen = param.length;
    //
    //var iStart = query.indexOf(param);
    //
    //if (iStart == -1){
    //    return "";
    //}
    //
    //iStart += iLen + 1;
    //var iEnd = query.indexOf("&", iStart);
    //if (iEnd == -1){
    //    return query.substring(iStart);
    //}
    //
    //return query.substring(iStart, iEnd);
}


function bundleFilePreview(fileTag, previewTag, clickRemoveable){

    $(fileTag).on("change", function(){
        // Get a reference to the fileList
        var files = !!this.files ? this.files : [];

        // If no files were selected, or no FileReader support, return
        if (!files.length || !window.FileReader) {
            $(previewTag).hide();
            return;
        }

        // Only proceed if the selected file is an image
        if (/^image/.test( files[0].type)){

            // Create a new instance of the FileReader
            var reader = new FileReader();

            // Read the local file as a DataURL
            reader.readAsDataURL(files[0]);

            // When loaded, set image data as background of div
            reader.onloadend = function(){

                $(previewTag).css("background-image", "url("+this.result+")");
                $(previewTag).show();

            }

        }

    });

    if(clickRemoveable){
        $(previewTag).click(function(){

            $(fileTag).val("");
            $(previewTag).hide();

        });
    }

}

function formatGame(data) {
    if (!data.id) {
        return data.text;
    }

    var html = '<span><img src="admin/game/getGameIcon?gameID='+data.id+'" width="15px" height="15px" /> ' + data.text + '</span>';

    var $state = $(html);
    return $state;
}

function reloadGames(selector, selectedID) {
    $.get('admin/game/getAllPermittedGames',{}, function (result) {
        var itemList = [];
        if(result.code == 0){
            var lst = result.data;
            var itemIndex;
            for(itemIndex in lst){
                if(lst[itemIndex].appID == selectedID){
                    itemList.unshift({id:lst[itemIndex].appID, text:lst[itemIndex].name});
                }else{
                    itemList.push({id:lst[itemIndex].appID, text:lst[itemIndex].name});
                }
            }
            gameLst = itemList;
        }else if(result.code == 2){
            parent.location.href = "login.html";
        }
        $(selector).select2({
            data:itemList,
            templateResult: formatGame,
            templateSelection:formatGame
        });
        $(selector).trigger('change');
    },'json');
}



/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function clearcontent() {
    $("#description").html("");
    $("#rt").html("");
    $("#URL").html("");
    $("#ExURL").html("");
    $("#ExResponse").html("");
    $("#params").html("");
    $("#command").html("");
}
function loadcontent(name,command) {
    
    clearcontent();

    $.ajax({
        type: "GET",
        url: "xml/" + name + ".xml",
        dataType: "xml",
        success: function(xml) {

            $(xml).find('root').each(function() {
                var $item = $(this);
                var description = $item.find('description').text().trim();
                var rt = $item.find('rt').text().trim();
                var ExURL = $item.find('ExURL');
                var ExResponse = $item.find('ExResponse');
                var paramHtml = "";
                var urlhtml = "<h3>URL: </h3>";


                $($item).find('URL').each(function() {

                    var cURL = $(this).text().trim();

                    urlhtml = urlhtml
                            + "<pre>"
                            + cURL
                            + "</pre>";

                });
                $($item).find('parameter').each(function() {

                    var name = $(this).find('name').text().trim();
                    var status = $(this).find('status').text().trim();
                    var paramdesc = $(this).find('paramdesc').text().trim();
                    var example = $(this).find('example').text().trim();
                    var s = "";
                    if (status === "required") {
                        s = "<span class=\"label label-danger\">Required</span>";
                    } else {
                        s = "<span class=\"label label-success\">Optional</span>";
                    }
                    paramHtml = paramHtml
                            + "<tr>"
                            + "<td>" + name + "</td>"
                            + "<td>" + s + "</td>"
                            + "<td>" + paramdesc + "</td>"
                            + "<td>" + example + "</td>"
                            + "</tr>";
                });

                $("#description").html(description);
                $("#rt").html(rt);
                $("#URL").html(urlhtml);
                $("#ExURL").html(ExURL);
                $("#ExResponse").html(ExResponse);
                $("#params").html(paramHtml);
                $("#command").html(command);
            });
        }
    });



}
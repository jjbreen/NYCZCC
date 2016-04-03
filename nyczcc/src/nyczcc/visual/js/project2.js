/**
 * Created by HuanYe on 16/4/3.
 */

var current_date ;
function initiate()
{
    $('.cal1').fullCalendar({
        year: 2012,
        month: 4,
        date: 25
    });

    $("td.fc-day-number").on("click",function(d)
    {
        d.target.cursor="pointer";
        current_date=d.target.getAttribute("data-date");
        console.log(current_date);
        parseData2Map(current_date);
    })
};

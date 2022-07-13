
/*
Helper function to get the website name from a url
*/
function getWebsiteImage(bookUrl) {

    let websiteName;
    if (bookUrl.includes("dauntbooks") || bookUrl.includes("blackwells"))
        websiteName = bookUrl.substring(8, bookUrl.indexOf("."));
    else
        websiteName = bookUrl.split(".")[1];

    return websiteName;
}
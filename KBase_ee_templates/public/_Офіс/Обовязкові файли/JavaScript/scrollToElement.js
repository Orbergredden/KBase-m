function scrollToElement(theElement) {
    if (typeof theElement === "string") theElement = document.getElementById(theElement);

    var selectedPosX = 0;   
    var selectedPosY = -20;   // что бы было чуть выше

    while (theElement != null) {
        selectedPosX += theElement.offsetLeft;
        selectedPosY += theElement.offsetTop;
        theElement = theElement.offsetParent;
    }

    window.scrollTo(selectedPosX, selectedPosY);
}
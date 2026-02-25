package com.mycompany.cardcreator.Modules;

enum CardElementType {
    TEXT, IMAGE
}

class CardElement {

    public Rect rect; // Position and size of box on the card

    CardElementType type;

}

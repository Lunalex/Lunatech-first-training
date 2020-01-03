package actors;

public enum PictureUpdateAnswerMessage
{
    NO_MESSAGE("noMessage"),
    NO_NEW_PICTURE("noNewPicture"),
    PICTURE_UPDATED("pictureUpdated"),
    PICTURE_NOT_UPDATED("pictureNotUpdated");

    private String message;

    PictureUpdateAnswerMessage(String message){
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}


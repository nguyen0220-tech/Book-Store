package catholic.ac.kr.secureuserapp.exception;

public class AlreadyExistsException extends RuntimeException{
    public AlreadyExistsException(){
        super();
    }
    public AlreadyExistsException(String message){
        super(message);
    }

    }

package kom.promise;

class PromiseEventFactory {

    // todo: create object pool

    static <T> SuccessEvent<T> getSuccessEvent(T data) {
        return new SuccessEvent<T>(data);
    }

    static FailEvent getFailEvent(Object data) {
        return new FailEvent(data);
    }

    static CancelEvent getCancelEvent(Object data) {
        return new CancelEvent(data);
    }

    static ProgressEvent getProgressEvent(Object data) {
        return new ProgressEvent(data);
    }
}

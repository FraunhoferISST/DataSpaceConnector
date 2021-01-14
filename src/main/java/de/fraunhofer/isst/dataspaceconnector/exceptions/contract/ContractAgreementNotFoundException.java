package de.fraunhofer.isst.dataspaceconnector.exceptions.contract;

public class ContractAgreementNotFoundException extends ContractException {

    /**
     * Construct an UnsupportedPatternException with the specified detail message and cause.
     *
     * @param msg The detail message.
     */
    public ContractAgreementNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Construct an UnsupportedPatternException with the specified detail message and cause.
     *
     * @param msg   The detail message.
     * @param cause The cause.
     */
    public ContractAgreementNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

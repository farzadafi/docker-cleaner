package ir.farzadafi.model.enumeration;

import lombok.Getter;

@Getter
public enum SmellType {

    APT_GET_UPDATE_ALONE("apt-get update used alone"),
    APT_GET_NO_INSTALL_RECOMMENDS_MISSING("Missing --no-install-recommends"),
    ADD_INSTEAD_OF_COPY_OR_WGET("ADD used instead of COPY or wget/curl"),
    LAST_USER_IS_ROOT("Container runs as root"),
    HAVE_SECRETS_IN_ENV("Secrets stored in ENV variables");

    private final String title;

    SmellType(String title) {
        this.title = title;
    }
}
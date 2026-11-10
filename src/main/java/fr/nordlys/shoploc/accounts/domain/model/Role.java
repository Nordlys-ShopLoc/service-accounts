package fr.nordlys.shoploc.accounts.domain.model;

/** Role of a user on the platform, one per actor of the specification. */
public enum Role {
    CLIENT,
    MERCHANT,
    /** City administrator: indicators, campaigns, benefits settings. */
    CITY_ADMIN,
    /** Municipal police officer: parking checks. */
    POLICE,
    /** ShopLoc administrator: merchant approval, accounts, cities. */
    ADMIN
}

import { IconDefinition } from '@fortawesome/angular-fontawesome';
import { faCartShopping, faTruckFast, faUser, faBars, faSun, faMoon, faCheck, faCircleCheck, faXmark, faEnvelope, faCode } from '@fortawesome/free-solid-svg-icons';
import { faGithub, faLinkedin } from '@fortawesome/free-brands-svg-icons';
import { faCopyright } from '@fortawesome/free-regular-svg-icons';

export const fontAwesomeIcons: IconDefinition[] = [
    // Brands
    faGithub,
    faLinkedin,
    faEnvelope,

    // faXing,
    // faTwitter,
    // faYoutube,
    // faFacebook,

    faUser,
    faBars,
    faSun,
    faMoon,
    
    faCopyright,
    faCode,
    faTruckFast,

    // Not used yet
    faXmark,
    faCheck,
    faCircleCheck,

    // won't be used in the near future
    faCartShopping,
];
(function ($) {
    "use strict";
    if (isSettings.lang === 'fr') {
        jQuery.timeago.settings.strings = {
            prefixAgo: "il y a",
            prefixFromNow: "dans",
            seconds: "moins d'une minute",
            minute: "une minute",
            minutes: "%d minutes",
            hour: "une heure",
            hours: "%d heures",
            day: "un jour",
            days: "%d jours",
            month: "un mois",
            months: "%d mois",
            year: "un an",
            years: "%d ans"
        };
    }
})(jQuery);
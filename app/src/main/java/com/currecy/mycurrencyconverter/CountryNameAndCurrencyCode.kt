package com.currecy.mycurrencyconverter

class CountryNameAndCurrencyCode(countryName: String, currencyCode: String) {
    val options = listOf(
        "aed", "afn", "all", "amd", "ang", "aoa", "ars", "aud", "awg", "azm", "azn", "bam",
        "bbd", "bdt", "bef", "bgn", "bhd", "bif", "bmd", "bnd", "bob", "brl", "bsd", "btn",
        "bwp", "byn", "byr", "bzd", "cad", "cdf", "chf", "clp", "cnh", "cny", "cop", "crc",
        // and so on...
    )

    val countryNames = listOf(
        "United Arab Emirates", "Afghanistan", "Albania", "Armenia", "Netherlands Antilles",
        "Angola", "Argentina", "Australia", "Aruba", "Azerbaijan", "Azerbaijan", "Bosnia and Herzegovina",
        "Barbados", "Bangladesh", "Belgium", "Bulgaria",
        "Bahrain", "Burundi", "Bermuda", "Brunei", "Bolivia", "Brazil", "Bahamas", "Bhutan",
        "Botswana", "Belarus", "Belarus", "Belize", "Canada", "Congo", "Switzerland", "Chile",
        "China", "China", "Colombia", "Costa Rica",
        // and so on...
    )

    fun createCountryCurrencyList(): List<CountryNameAndCurrencyCode> {
        return options.zip(countryNames) { code, country ->
            CountryNameAndCurrencyCode(country, code.uppercase())
        }
    }

}
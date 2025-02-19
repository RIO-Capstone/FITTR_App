import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegistrationViewModel : ViewModel() {
    private val _firstName = MutableLiveData("")
    val first_name: LiveData<String> get() = _firstName

    private val _lastName = MutableLiveData("")
    val last_name: LiveData<String> get() = _lastName

    private val _email = MutableLiveData("")
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData("")
    val password: LiveData<String> get() = _password

    private val _weight = MutableLiveData(0)
    val weight: LiveData<Int> get() = _weight

    private val _height = MutableLiveData(0)
    val height: LiveData<Int> get() = _height

    private val _phoneNumber = MutableLiveData("")
    val phone_number: LiveData<String> get() = _phoneNumber

    private val _gender = MutableLiveData("")
    val gender: LiveData<String> get() = _gender

    private val _dateOfBirth = MutableLiveData("")
    val date_of_birth: LiveData<String> get() = _dateOfBirth

    private val _productId = MutableLiveData(0)
    val product_id: LiveData<Int> get() = _productId

    private val _fitnessGoal = MutableLiveData("")
    val fitness_goal: LiveData<String> get() = _fitnessGoal

    // Update methods for encapsulated properties
    fun setFirstName(value: String) {
        _firstName.value = value
    }

    fun setLastName(value: String) {
        _lastName.value = value
    }

    fun setEmail(value: String) {
        _email.value = value
    }

    fun setPassword(value: String) {
        _password.value = value
    }

    fun setWeight(value: Int) {
        _weight.value = value
    }

    fun setHeight(value: Int) {
        _height.value = value
    }

    fun setPhoneNumber(value: String) {
        _phoneNumber.value = value
    }

    fun setGender(value: String) {
        _gender.value = value
    }

    fun setDateOfBirth(value: String) {
        _dateOfBirth.value = value
    }

    fun setProductId(value: Int) {
        _productId.value = value
    }

    fun setFitnessGoal(value:String){
        _fitnessGoal.value = value
    }

    fun getRegistrationData(): Map<String, Any> {
        return mapOf(
            "first_name" to (_firstName.value ?: ""),
            "last_name" to (_lastName.value ?: ""),
            "email" to (_email.value ?: ""),
            "password" to (_password.value ?: ""),
            "weight" to (_weight.value ?: 0),
            "height" to (_height.value ?: 0),
            "phone_number" to (_phoneNumber.value ?: ""),
            "gender" to (_gender.value ?: ""),
            "date_of_birth" to (_dateOfBirth.value ?: ""),
            "product_id" to (_productId.value ?: 0),
            "fitness_goal" to (_fitnessGoal.value ?: ""),
        )
    }

}

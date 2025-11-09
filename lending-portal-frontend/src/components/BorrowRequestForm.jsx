/**
 * BorrowRequestForm
 *
 * A controlled form component used to submit a borrow request for equipment.
 * This component is presentation-focused and delegates state updates and
 * submission handling to the parent via the `onChange` and `onSubmit` props.
 *
 * Props:
 * @param {Array<Object>} items - Array of equipment objects to choose from. Each item should include { id, itemName, availableQuantity }.
 * @param {Object} formState - Current form values: { equipmentId, startDate, endDate, qty }.
 * @param {function(Object):void} onChange - Handler called with the new form state when any field changes.
 * @param {function(Event):void} onSubmit - Form submit handler.
 *
 * Returns JSX form elements.
 */
const BorrowRequestForm = ({ items, formState, onChange, onSubmit, formErrors = {} }) => {
  /**
   * Create a change handler for a specific field.
   * @param {string} field - The form field name to update (e.g. 'startDate').
   * @returns {function(Event):void} - Event handler which calls `onChange` with updated state.
   */
  const updateField = (field) => (event) => {
    onChange({ ...formState, [field]: event.target.value });
  };

  return (
    <div className="bg-white shadow rounded-lg p-6 mt-6">
      <h2 className="text-xl font-semibold mb-3">Borrow something</h2>
      <form onSubmit={onSubmit} className="space-y-3">
        <div>
          <label className="block text-sm font-medium text-slate-700">Select item</label>
          <select
            value={formState.equipmentId}
            onChange={updateField("equipmentId")}
            required
            className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2 focus:ring-2 focus:ring-sky-500"
          >
            <option value="">-- Choose --</option>
            {items.map((equip) => (
              <option key={equip.id} value={equip.id}>
                {equip.itemName} ({equip.availableQuantity} free)
              </option>
            ))}
          </select>
          {formErrors.equipmentId && <p className="text-xs text-red-600 mt-1">{formErrors.equipmentId}</p>}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div>
            <label className="block text-sm font-medium text-slate-700">Start Date</label>
            <input type="date" value={formState.startDate} onChange={updateField("startDate")} required className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
            {formErrors.startDate && <p className="text-xs text-red-600 mt-1">{formErrors.startDate}</p>}
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700">End Date</label>
            <input type="date" value={formState.endDate} onChange={updateField("endDate")} required className="mt-1 block w-full rounded-md border-slate-200 px-3 py-2" />
            {formErrors.endDate && <p className="text-xs text-red-600 mt-1">{formErrors.endDate}</p>}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700">Quantity</label>
          <input type="number" min="1" value={formState.qty} onChange={updateField("qty")} className="mt-1 block w-28 rounded-md border-slate-200 px-3 py-2" />
          {formErrors.qty && <p className="text-xs text-red-600 mt-1">{formErrors.qty}</p>}
        </div>

        <div>
          <button type="submit" className="w-full bg-sky-600 hover:bg-sky-700 text-white py-2 rounded-md font-semibold">
            Submit Request
          </button>
        </div>
      </form>
    </div>
  );
};

export default BorrowRequestForm;

package fashionstorebackend.exception;

public class InventoryNotFoundException extends RuntimeException {

    public InventoryNotFoundException(Long id) {
        super("Inventory not found with id: " + id);
    }
}
package sample;

import java.util.List;

public interface SerialObserver {

    void handle(final List<Byte> readBuffer);
}

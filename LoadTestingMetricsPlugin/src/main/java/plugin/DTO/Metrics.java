package plugin.DTO;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Metrics {

    private double _httpReqDurationAvg;
    private double _httpReqDurationP95;
    private double _httpReqDurationMax;

    private int _iterationsCount;
    private int _httpReqsCount;

    private int _checksPasses;
    private double _httpReqFailedValue;

    private int _dataReceivedCount; // в килобайтах
    private int _vusMaxValue;

    private double _httpReqWaitingMax;
}
